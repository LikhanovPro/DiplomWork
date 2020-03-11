package main.requestObject.post;

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
}
