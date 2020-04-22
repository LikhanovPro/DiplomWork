package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Map;

public class PostGetMyPosts implements ResponseApi {

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList<Map<Object, Object>> posts = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Map<Object, Object>> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Map<Object, Object>> posts) {
        this.posts = posts;
    }
}
