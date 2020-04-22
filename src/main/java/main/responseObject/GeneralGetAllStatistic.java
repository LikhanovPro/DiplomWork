package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralGetAllStatistic implements ResponseApi {

    @JsonProperty
    int postsCount;

    @JsonProperty
    int likesCount;

    @JsonProperty
    int dislikesCount;

    @JsonProperty
    int viewsCount;

    @JsonProperty
    String firstPublication;

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public String getFirstPublication() {
        return firstPublication;
    }

    public void setFirstPublication(String firstPublication) {
        this.firstPublication = firstPublication;
    }
}
