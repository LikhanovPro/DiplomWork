package main.requestObject.post;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostPostLikeObject {

    @JsonProperty ("post_id")
    Integer postId;

    public Integer getPostId() {
        return postId;
    }
}
