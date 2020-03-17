package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AuthPostRegister {

    @JsonProperty
    boolean result;

    @JsonProperty
    Map<Object, Object> errors = new HashMap<>();

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
}
