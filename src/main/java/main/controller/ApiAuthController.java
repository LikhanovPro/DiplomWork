package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping ("/api/auth")
public class ApiAuthController extends HttpServlet {

    //Подключаем репозитории
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    //======================================================

    //Создаем объект, общий для всего контроллера
    private static Map<String, Integer> sessionInformation = new HashMap<>();
    //======================================================

    //Задаем почту и пароль к eMail Support с которого будет рассылка почты
    private static String supportMail = "DiplomWorkSup@gmail.com";
    private static String supportMailPassword = "123DiplomWork";
    //=======================================================

    //Котроллер входа под своей учетной записью
    @PostMapping("/login")
    public String authLogin(@RequestBody Map<String, String> information, HttpServletRequest request) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        String eMail = information.get("e_mail");
        String password = information.get("password");

        //Выполняем поиск юзера по eMail и password
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                if (user.getPassword().equals(password)) {
                    //Заполняем Json информацией о пользователе
                    answerJson = MetodsForAuthController.createAuthInformation(user, postsRepository);

                    int sessionRandomInt = (int) (Math.random() * 1000);
                    session.setAttribute("name", user.getName() + sessionRandomInt); //Создали случайным образом имя ссесии
                    sessionInformation.put((String) session.getAttribute("name"), user.getId()); // Под случайным именем сессии зафиксировали id текущего пользователя
                    return new Gson().toJson(answerJson);
                }
            }
        }
        //Если пользоваетель не найден, либо неверно введен пароль
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    //Контроллер првоерки текущего пользователя
    @GetMapping("/check")
    public String authCheckLogin(HttpServletRequest request) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        //Проверка наличия сессии, т.е. есть ли авторизованный пользователь
        if (sessionInformation.containsKey((String) session.getAttribute("name"))) {
            answerJson = MetodsForAuthController.createAuthInformation(usersRepository.findById(sessionInformation.get((String) session.getAttribute("name"))).get(), postsRepository);
            return new Gson().toJson(answerJson);
        }

        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    //Контроллер выхода из авторизации
    @GetMapping("/logout")
    public String authLogout(HttpServletRequest request) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        sessionInformation.remove((String) session.getAttribute("name")); // Из текущей сессии получаем id авторизованного пользователя и удаляем его из сессии
        answerJson.put("result", true);
        return new Gson().toJson(answerJson);
    }

    //Контроллер регистрации пользователя
    @PostMapping("/register")
    public String createNewUser(@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<Object, Object> errors = new HashMap<Object, Object>();
        int codeLength = 6; //Минлимальная длина пароля (password)

        //Информация в запросе передается в формате Json
        String eMail = information.get("e_mail");
        String password = information.get("password");
        String captcha = information.get("captcha");
        String captchaSecret = information.get("captcha_secret");

        //Проверка существования уже зарегистрированного Email
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                answerJson.put("result", false);
                errors.put("email", "Этот e-mail уже зарегистрирован");
                answerJson.put("errors", errors);
                return new Gson().toJson(answerJson);
            }
        }
        //Проверка длины кода не менее 6 знаков
        if (password.length() < codeLength) {
            answerJson.put("result", false);
            errors.put("password", "Пароль короче 6-ти символов");
            answerJson.put("errors", errors);
            return new Gson().toJson(answerJson);
        }

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
                    answerJson.put("result", true);
                    return new Gson().toJson(answerJson);
                }
                else {
                    answerJson.put("result", false);
                    errors.put("captcha", "Код с картинки введен неверно");
                    answerJson.put("errors", errors);
                    return new Gson().toJson(answerJson);
                }
            }

        }
        answerJson.put("result", false);
        errors.put("captcha", "Ошибка генерации кода captcha");
        answerJson.put("errors", errors);
        return new Gson().toJson(answerJson);
    }

    //Создание captcha кода, запись его в БД
    @GetMapping("/captcha")
    public String createCaptcha () throws IOException {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        //Позиция необходимой информации в файле возвращаемом методом: ArrayList <String> createdCaptcha = MetodsForAuthController.metodsCreateCaptcha();
        int codeIndex = 0;
        int secretCodeIndex = 1;
        int captchaIndex = 2;
        //Продолжительность жизни captcha кода в часах
        int lifeTime = 1;

        //Проверка устаревания уже имеющихся кодов captcha
        captchaCodesRepository.findAll().forEach(captchaCodes -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -lifeTime);//текущая дата минус время жизни captcha кода
            Date date = calendar.getTime();
            if (captchaCodes.getTime().before(date)) {
                captchaCodesRepository.delete(captchaCodes);//Удаление устаревшего кода
            }
        });

        ArrayList <String> createdCaptcha = MetodsForAuthController.metodsCreateCaptcha(); //Метод созжания captcha кода случайным образом
        CaptchaCodes captchaCodes = new CaptchaCodes();
        captchaCodes.setCode(createdCaptcha.get(codeIndex)); //Получаем сам код
        captchaCodes.setSecretCode(createdCaptcha.get(secretCodeIndex)); //Получаем секретный код
        captchaCodes.setTime(new Date()); // Устанавливаем дату создания кода, что бы следить за устареванием
        captchaCodesRepository.save(captchaCodes);

        answerJson.put("secret", createdCaptcha.get(secretCodeIndex)); //Возвращаем секретный код от сервера на frontend
        answerJson.put("image", createdCaptcha.get(captchaIndex)); //Возвращаем картинку(зашифрованный код) на frontend для отображения на странице
        return new Gson().toJson(answerJson);
    }

    //Контроллер восстановления пароля
    @PostMapping("/restore")
    public String restorePasswords (@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        String eMail = information.get("email"); // получаем eMail пользователя, чей пароль будет восстанавливаться
        String restoreCode = MetodsForAuthController.generateRestoreCode(); //получаем случайно сгенерированный код восстановления пароля

        //Ищем пользователя в БД по его eMail
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                user.setCode(restoreCode);
                //Если пользователь найден, то отправляем ему на почту ссылку восстановления пароля
                MetodsForAuthController.sendMail(eMail, restoreCode, supportMail, supportMailPassword);
                usersRepository.save(user);//Сохраняем в БД для пользователя секретный код восстановления пароля
                answerJson.put("result", true);
                return new Gson().toJson(answerJson);
            }
        }
        answerJson.put("result", false);//Пользователь с таким eMail не найден
        return new Gson().toJson(answerJson);
    }

    //Контроллер изменения пароля (password) пользователя
    @PostMapping("/password")
    public String createNewPassword (@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<Object, Object> errors = new HashMap<Object, Object>();
        int codeLength = 6; //Минимальная длина кода (password)

        //Информация с frontend приходит в виде Json файла
        String code = information.get("code");
        String password = information.get("password");
        String captcha = information.get("captcha");//Captcha код, который ввел пользователь
        String captchaSecret = information.get("captcha_secret");//Секретный код captcha, переданный с frontend

        //Проверка длины пароля
        if (password.length() < codeLength) {
            answerJson.put("result", false);
            errors.put("password", "Пароль короче 6-ти символов");
            answerJson.put("errors", errors);
            return new Gson().toJson(answerJson);
        }

        //Проверка соответствия captcha кода
        for (CaptchaCodes captchaCodes : captchaCodesRepository.findAll()) {
            if (captchaCodes.getSecretCode().equals(captchaSecret)) {//поиск в БД captcha кода по секретному captcha коду, переданному с frontend
                if (!captchaCodes.getCode().equals(captcha)) {//Проверка соответствия введенного captcha, хранящемуся в БД
                    answerJson.put("result", false);
                    errors.put("captcha", "Код с картинки введен неверно");
                    answerJson.put("errors", errors);
                    return new Gson().toJson(answerJson);
                }
            }
        }

        //Если предыдущее условие выполнено, т.е. captcha введен верно
        //Проверем код восстановления пользователя, хранящегося в БД коду восстановления, переданному с frontend
        for (Users user : usersRepository.findAll()) {
            if (user.getCode().equals(code)) {
                user.setPassword(password);
                usersRepository.save(user);
                answerJson.put("result", true);
                }
            else {
                answerJson.put("result", false);
                errors.put("code", "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">Запросить ссылку снова</a>");
                answerJson.put("errors", errors);
            }
        }
        return new Gson().toJson(answerJson);
    }



    //Геттеры и Сеттеры, необходимые для реализации методов Класса

    public Map<String, Integer> getSessionInformation() {
        return sessionInformation;
    }

    public void setSessionInformation(Map<String, Integer> sessionInformation) {
        this.sessionInformation = sessionInformation;
    }

    public static Integer getIdUserLogin (HttpServletRequest request) {
        HttpSession session = request.getSession();

        return sessionInformation.get((String) session.getAttribute("name"));
    }
}
