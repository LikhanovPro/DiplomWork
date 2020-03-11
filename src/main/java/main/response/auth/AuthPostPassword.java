package main.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.models.*;
import main.requestObject.auth.AuthPostPasswordObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class AuthPostPassword {

    //Подключаем репозитории
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    //======================================================

    @JsonProperty
    boolean result;

    @JsonProperty
    Map<Object, Object> errors = new HashMap<Object, Object>();

    int codeLength = 6; //Минимальная длина кода (password)

    private AuthPostPassword (AuthPostPasswordObject information) {

        //Информация с frontend приходит в виде Json файла
        String code = information.getCode();
        String password = information.getPassword();
        String captcha = information.getCaptcha();//Captcha код, который ввел пользователь
        String captchaSecret = information.getCaptcha_secret();//Секретный код captcha, переданный с frontend

        //Проверка длины пароля
        if (password.length() < codeLength) {
            result = false;
            errors.put("password", "Пароль короче 6-ти символов");
        }

        //Проверка соответствия captcha кода
        for (CaptchaCodes captchaCodes : captchaCodesRepository.findAll()) {
            if (captchaCodes.getSecretCode().equals(captchaSecret)) {//поиск в БД captcha кода по секретному captcha коду, переданному с frontend
                if (!captchaCodes.getCode().equals(captcha)) {//Проверка соответствия введенного captcha, хранящемуся в БД
                    result = false;
                    errors.put("captcha", "Код с картинки введен неверно");
                }
            }
        }

        //Если предыдущее условие выполнено, т.е. captcha введен верно
        //Проверем код восстановления пользователя, хранящегося в БД коду восстановления, переданному с frontend
        for (Users user : usersRepository.findAll()) {
            //Добавлена проверка на null, т.к. возникали ошибки при переборе юзеров
            if (user.getCode() != null && user.getCode().equals(code)) {
                user.setPassword(password);
                usersRepository.save(user);
                result = true;
            } else {
                result = false;
                errors.put("code", "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">Запросить ссылку снова</a>");
            }
        }
    }

    public AuthPostPassword () {}

    public ResponseEntity getAuthPostPassword (AuthPostPasswordObject information) {
        return ResponseEntity.status(HttpStatus.OK).body(new AuthPostPassword(information));
    }
}
