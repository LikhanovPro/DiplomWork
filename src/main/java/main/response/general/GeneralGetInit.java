package main.response.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private GeneralGetInit (String title, String subtitle, String phone, String email, String copyright, String copyrightFrom) {
        this.title = title;
        this.subtitle = subtitle;
        this.phone = phone;
        this.email = email;
        this.copyright = copyright;
        this.copyrightFrom = copyrightFrom;
    }

    public GeneralGetInit () {}

    public ResponseEntity getGeneralInit (String title, String subtitle, String phone, String email, String copyright, String copyrightFrom) {

        return ResponseEntity.status(HttpStatus.OK).body(new GeneralGetInit(title, subtitle, phone, email, copyright, copyrightFrom));
    }
}
