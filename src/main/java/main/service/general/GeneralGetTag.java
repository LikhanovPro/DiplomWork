package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.models.Posts;
import main.models.TagsRepository;

import java.util.*;

public class GeneralGetTag {

    @JsonProperty
    ArrayList <Map> tags = new ArrayList<Map>();

    public GeneralGetTag (TagsRepository tagsRepository) {
        Map <String, Integer> tagCount = new HashMap<>();
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
            this.tags.add(tagWeight);
        });
    }
}
