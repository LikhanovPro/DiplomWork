package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthGetCaptcha implements ResponseApi {

    @JsonProperty
    String secret;

    @JsonProperty
    String image;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
