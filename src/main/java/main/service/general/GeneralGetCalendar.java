package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import main.controller.MetodsForGeneralController;
import main.models.PostsRepository;

import java.text.SimpleDateFormat;
import java.util.*;

public class GeneralGetCalendar {

    @JsonProperty
    Set<Integer> years = new TreeSet<>();

    @JsonProperty
    Map <Object, Integer> postsMapForYears = new HashMap<>();

    public GeneralGetCalendar (String year, PostsRepository postsRepository) {
        SimpleDateFormat dateFormatForYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (year.length() != 4 || year.isEmpty()) {
            year = dateFormat.format(new Date());
        }
        years.add(Integer.parseInt(dateFormatForYear.format(year)));

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
    }
}
