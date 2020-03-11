package main.requestObject.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthPostRegisterObject {

    @JsonProperty ("e_mail")
    String eMail;

    String password;

    String captcha;

    String captcha_secret;

    public String geteMail() {
        return eMail;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public String getCaptcha_secret() {
        return captcha_secret;
    }
}
