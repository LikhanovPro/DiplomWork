package main.requestObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralPostCommentObject {

    @JsonProperty("parent_id")
    Integer parentId;

    @JsonProperty ("post_id")
    Integer postId;

    @JsonProperty
    String text;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
