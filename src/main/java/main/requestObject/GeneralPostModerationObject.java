package main.requestObject;

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

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
