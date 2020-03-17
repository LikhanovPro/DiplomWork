package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

public class GeneralGetCalendar {

    @JsonProperty
    Set<Integer> years = new TreeSet<>();

    @JsonProperty
    Map <Object, Integer> posts = new HashMap<>();

    public Set<Integer> getYears() {
        return years;
    }

    public void setYears(Set<Integer> years) {
        this.years = years;
    }

    public Map<Object, Integer> getPosts() {
        return posts;
    }

    public void setPosts(Map<Object, Integer> posts) {
        this.posts = posts;
    }
}
