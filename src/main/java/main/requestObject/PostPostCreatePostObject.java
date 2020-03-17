package main.requestObject;

import java.util.ArrayList;

public class PostPostCreatePostObject {

    private Integer active;
    private String title;
    private String text;
    private ArrayList<String> tags;
    private String time;

    public Integer getActive() {
        return active;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getTime() {
        return time;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
