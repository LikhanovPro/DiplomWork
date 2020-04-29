package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Map;

public class PostGetMyPosts implements ResponseApi {

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList<Map<Object, Object>> posts = new ArrayList<>();

    @JsonProperty
    String message;

    @JsonProperty
    boolean result;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
