package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class PostPostCreatePost implements ResponseApi {

    @JsonProperty
    boolean result;

    @JsonProperty
    Map <String, String> errors = new HashMap<>();

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
