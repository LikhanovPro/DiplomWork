package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostPostDislike implements ResponseApi {

    @JsonProperty
    boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
