package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AuthPostPassword implements ResponseApi {

    @JsonProperty
    boolean result;

    @JsonProperty
    String message;

    @JsonProperty
    Map<Object, Object> errors = new HashMap<Object, Object>();

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Map<Object, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<Object, Object> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
