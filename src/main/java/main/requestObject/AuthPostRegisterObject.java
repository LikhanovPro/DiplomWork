package main.requestObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthPostRegisterObject {

    @JsonProperty ("e_mail")
    String eMail;

    String password;

    String captcha;

    String captcha_secret;

    String name;

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

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public void setCaptcha_secret(String captcha_secret) {
        this.captcha_secret = captcha_secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
