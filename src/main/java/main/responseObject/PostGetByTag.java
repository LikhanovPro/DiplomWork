package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Map;

public class PostGetByTag implements ResponseApi {
    @JsonProperty
    int count;

    @JsonProperty
    ArrayList <Map> posts = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Map> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Map> posts) {
        this.posts = posts;
    }
}
