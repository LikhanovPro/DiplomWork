package main.requestObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostPostDislikeObject {

    @JsonProperty ("post_id")
    Integer postId;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }
}
