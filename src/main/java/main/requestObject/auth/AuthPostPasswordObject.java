package main.requestObject.auth;

public class AuthPostPasswordObject {

    String code;
    String password;
    String captcha;//Captcha код, который ввел пользователь
    String captcha_secret;//Секретный код captcha, переданный с frontend

    public String getCode() {
        return code;
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
