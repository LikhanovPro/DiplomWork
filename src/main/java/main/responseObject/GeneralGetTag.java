package main.responseObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class GeneralGetTag {

    @JsonProperty
    ArrayList <Map> tags = new ArrayList<Map>();

    public ArrayList<Map> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Map> tags) {
        this.tags = tags;
    }
}
