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
}
