package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AuthGetCheck {

    @JsonProperty
    boolean result;
    @JsonProperty
    Map<Object, Object> user = new HashMap<>();

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Map<Object, Object> getUser() {
        return user;
    }

    public void setUser(Map<Object, Object> user) {
        this.user = user;
    }
}
