package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthPostRestore {

    @JsonProperty
    boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}