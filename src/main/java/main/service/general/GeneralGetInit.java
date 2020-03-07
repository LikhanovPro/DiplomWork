package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralGetInit {

    @JsonProperty
    String title;

    @JsonProperty
    String subtitle;

    @JsonProperty
    String phone;

    @JsonProperty
    String email;

    @JsonProperty
    String copyright;

    @JsonProperty
    String copyrightFrom;

    public GeneralGetInit (String title, String subtitle, String phone, String email, String copyright, String copyrightFrom) {
        this.title = title;
        this.subtitle = subtitle;
        this.phone = phone;
        this.email = email;
        this.copyright = copyright;
        this.copyrightFrom = copyrightFrom;
    }
}
