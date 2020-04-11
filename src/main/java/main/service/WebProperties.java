package main.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "")
@Configuration("webProperties")
public class WebProperties {


    Integer titleLength;
    Integer textLength;
    Integer commentsLength;
    int lifeTime = 1;
    int codeLength = 6; //Минимальная длина кода (password)
    String pathToImages = "src/main/resources/static/upload/";
    String pathToAvatars = "src/main/resources/static/img/avatars/";
    String defaultAvatar = "src/main/resources/static/img/avatars/default.jpg";

    public Integer getTitleLength() {
        return titleLength;
    }

    public void setTitleLength(Integer titleLength) {
        this.titleLength = titleLength;
    }

    public Integer getTextLength() {
        return textLength;
    }

    public void setTextLength(Integer textLength) {
        this.textLength = textLength;
    }

    public Integer getCommentsLength() {
        return commentsLength;
    }

    public void setCommentsLength(Integer commentsLength) {
        this.commentsLength = commentsLength;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public String getPathToImages() {
        return pathToImages;
    }

    public void setPathToImages(String pathToImages) {
        this.pathToImages = pathToImages;
    }

    public String getPathToAvatars() {
        return pathToAvatars;
    }

    public void setPathToAvatars(String pathToAvatars) {
        this.pathToAvatars = pathToAvatars;
    }

    public String getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(String defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }
}
