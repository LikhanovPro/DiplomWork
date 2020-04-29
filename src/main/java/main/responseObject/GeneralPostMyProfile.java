package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralPostMyProfile implements ResponseApi {

    @JsonProperty
    boolean result;

    @JsonProperty
    String message;


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
