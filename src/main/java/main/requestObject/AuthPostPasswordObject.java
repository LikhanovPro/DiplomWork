package main.requestObject;

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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public void setCaptcha_secret(String captcha_secret) {
        this.captcha_secret = captcha_secret;
    }
}
