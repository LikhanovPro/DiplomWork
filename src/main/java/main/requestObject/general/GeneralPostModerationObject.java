package main.requestObject.general;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralPostModerationObject {

    @JsonProperty ("post_id")
    Integer postId;

    @JsonProperty ("decision")
    String newStatus;

    public Integer getPostId() {
        return postId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
