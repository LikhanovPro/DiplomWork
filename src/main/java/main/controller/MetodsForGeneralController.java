package main.controller;

import main.models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetodsForGeneralController {

    //Метод который расставляет веса тегам
    public static Map<Object, Object> createMapForTag (String query, TagsRepository tagsRepository) {
        Map <String, Integer> tagCount = new HashMap<>();
        Integer maxCount = 0;
        Map <Object, Object> tagWeight = new HashMap<>();

        for (Tags tag : tagsRepository.findAll()){//Для всех тегов
            if (tag.getName().contains(query)) {
                for (Posts post : tag.getPostsForTags()) {//Посты, которые содержат тег
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {//Проверка соответствия поста условиям публикации
                        if (tagCount.containsKey(tag.getName())) {
                            tagCount.replace(tag.getName(), tagCount.get(tag.getName()), tagCount.get(tag.getName()) + 1);//Увеличиваем определенному тегу его количество в постах
                            if (tagCount.get(tag.getName()) > maxCount) {
                                maxCount = tagCount.get(tag.getName());//Смотрим максимальное количество тегов, при необходимости меняем
                            }
                        } else {
                            tagCount.put(tag.getName(), 1);//Если тег встречается впервые, то вносим его в список и присваиваем значение равное 1
                        }
                    }
                }
            }
        }
        for (String tags : tagCount.keySet()) {
            tagWeight.put(tags, Double.valueOf(tagCount.get(tags)/maxCount));//Считаем веса тегов
        }
        return tagWeight;
    }

    //Метод создания перечня тегов без запроса с подсчетов месов
    public static ArrayList<Map> createMapForTagWithoutQuery (TagsRepository tagsRepository) {
        Map <String, Integer> tagCount = new HashMap<>();
        ArrayList <Map> arrayForAnswer = new ArrayList<>();
        Set<Integer> tagCounts = new TreeSet<>();

        //Считаем количество тегов вообще и сколь раз он встречается в постах
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
            tagWeight.put("weight", (double) tagCount.get(tag.getName())/tagCounts.stream().max(Integer::compareTo).get());//Считаем весса тегов
            arrayForAnswer.add(tagWeight);
         });
        return arrayForAnswer;
    }

    //Метод создания перечня постов по годам
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

    //Метод сбора информации по постам
    public static Map <Object, Object> postsStatistics (List <Posts> posts) {
        Map <Object, Object> answerForJson = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        answerForJson.put("postsCount", posts.size());
        int likeCount = 0;
        int dislikeCounts = 0;
        int viewCounts = 0;
        Date date = new Date();
        for (Posts post : posts) {
            viewCounts += post.getViewCount();
            if (!date.before(post.getTime())) {
                date = post.getTime();
            }
            for (PostsVotes votes : post.getVotesToPost()) {
                if (votes.isValue()) {
                    likeCount++;
                }
                else {
                    dislikeCounts++;
                }
            }
        }
        answerForJson.put("likesCount", likeCount);
        answerForJson.put("dislikesCount", dislikeCounts);
        answerForJson.put("viewsCount", viewCounts);
        answerForJson.put("firstPublication", dateFormat.format(date));
        return answerForJson;
    }


}
