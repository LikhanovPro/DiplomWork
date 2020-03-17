package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class PostGetById {

    @JsonProperty
    int id;

    @JsonProperty
    String time;

    @JsonProperty
    Map <Object, Object> user = new HashMap<>();

    @JsonProperty
    String title;

    @JsonProperty
    String text;

    @JsonProperty
    int likeCount;

    @JsonProperty
    int dislikeCount;

    @JsonProperty
    ArrayList<Map> comments = new ArrayList<>();

    @JsonProperty
    List<String> tags = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<Object, Object> getUser() {
        return user;
    }

    public void setUser(Map<Object, Object> user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public ArrayList<Map> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Map> comments) {
        this.comments = comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
