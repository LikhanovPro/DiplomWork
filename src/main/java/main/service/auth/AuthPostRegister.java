package main.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import main.models.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthPostRegister {

    @JsonProperty
    boolean result;
    @JsonProperty
    Map<Object, Object> errors = new HashMap<>();

    public void authPostRegister (Map<String, String> information, UsersRepository usersRepository, PostsRepository postsRepository,
                                  CaptchaCodesRepository captchaCodesRepository, int codeLength) {
        //Информация в запросе передается в формате Json
        String eMail = information.get("e_mail");
        String password = information.get("password");
        String captcha = information.get("captcha");
        String captchaSecret = information.get("captcha_secret");

        //Проверка существования уже зарегистрированного Email
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                this.result = false;
                errors.put("email", "Этот e-mail уже зарегистрирован");
            }
        }

        //Проверка длины кода не менее 6 знаков
        if (password.length() < codeLength) {
            this.result = false;
            errors.put("password", "Пароль короче 6-ти символов");
        }

        this.result = false;
        errors.put("captcha", "Ошибка генерации кода captcha");

        //Проверка соответствия captcha кода, который создается автоматически и записывается в БД
        for (CaptchaCodes captchaCodes : captchaCodesRepository.findAll()) {
            if (captchaCodes.getSecretCode().equals(captchaSecret)) {
                if (captchaCodes.getCode().equals(captcha)) {
                    Users newUser = new Users();
                    newUser.seteMail(eMail);
                    newUser.setModerator(false);
                    newUser.setName(eMail); //В форме регистрации нет поля name, предполагаю регистрацию с именем равным eMail
                    //а затем уже пользователь может его изменить
                    newUser.setPassword(password);
                    newUser.setRegTime(new Date());
                    usersRepository.save(newUser);
                    this.result = true;
                    errors.clear();
                }
                else {
                    this.result = false;
                    errors.put("captcha", "Код с картинки введен неверно");
                }
            }
        }
    }
}
