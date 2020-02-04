package main.controller;

import main.models.Posts;
import main.models.PostsRepository;
import main.models.TagsRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetodsForGeneralController {

    public static Map<Object, Object> createMapForTag (String query, TagsRepository tagsRepository) {
        Map <String, Integer> tagCount = new HashMap<>();
        Integer [] maxCount = new Integer [1];
        Map <Object, Object> tagWeight = new HashMap<>();

        tagsRepository.findAll().forEach(tag -> {
            if (tag.getName().contains(query)) {
                tag.getPostsForTags().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        if (tagCount.containsKey(tag.getName())) {
                            tagCount.replace(tag.getName(), tagCount.get(tag.getName()), tagCount.get(tag.getName()) + 1);
                            if (tagCount.get(tag.getName()) > maxCount[0]) {
                                maxCount[0] = tagCount.get(tag.getName());
                            }
                        } else {
                            tagCount.put(tag.getName(), 1);
                        }
                    }
                });
            }
        });
        tagCount.keySet().forEach(tags -> {
            tagWeight.put(tags, Double.valueOf(tagCount.get(tags)/maxCount[0]));
        });
        return tagWeight;
    }

    public static ArrayList<Map> createMapForTagWithoutQuery (TagsRepository tagsRepository) {
        Map <String, Integer> tagCount = new HashMap<>();
        ArrayList <Map> arrayForAnswer = new ArrayList<>();
        Set<Integer> tagCounts = new TreeSet<>();

        tagsRepository.findAll().forEach(tag -> {
            int i = 0;
            for (Posts post : tag.getPostsForTags()) {
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                    i++;
                }
            }
            tagCounts.add(i);
            tagCount.put(tag.getName(), i);
        });
        tagsRepository.findAll().forEach(tag -> {
            Map <Object, Object> tagWeight = new HashMap<>();
            tagWeight.put("name", tag.getName());
            tagWeight.put("weight", (double) tagCount.get(tag.getName())/tagCounts.stream().max(Integer::compareTo).get());
            arrayForAnswer.add(tagWeight);
         });
        return arrayForAnswer;
    }

    public static Map <Object, Object> createMapForGetCalendar (int year, PostsRepository postsRepository) {
        Set <Integer> years = new TreeSet<>();
        SimpleDateFormat dateFormatForYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map <Object, Object> answerForJson = new HashMap<>();
        Map <Object, Integer> postsMapForYears = new HashMap<>();

        postsRepository.findAll().forEach(post -> {

            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                years.add(Integer.parseInt(dateFormatForYear.format(post.getTime())));
                if (postsMapForYears.containsKey(dateFormat.format(post.getTime()))) {
                    postsMapForYears.replace(dateFormat.format(post.getTime()), postsMapForYears.get(dateFormat.format(post.getTime())),
                            postsMapForYears.get(dateFormat.format(post.getTime())) + 1);
                }
                else {
                    postsMapForYears.put(dateFormat.format(post.getTime()), 1);
                }
            }
        });
        answerForJson.put("years", years);
        answerForJson.put("posts", postsMapForYears);
        return answerForJson;

    }
}
