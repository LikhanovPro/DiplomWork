package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping ("/api/auth")
public class ApiAuthController extends HttpServlet {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;

    private static Map<String, Integer> sessionInformation = new HashMap<>();

    //Задаем почту и пароль к eMail Support с которого будет рассылка почты
    private static String supportMail = "DiplomWorkSup@gmail.com";
    private static String supportMailPassword = "123DiplomWork";
    //================================================================

    @PostMapping("/login")
    public String authLogin(@RequestBody Map<String, String> information, HttpServletRequest request) {
        String eMail = information.get("e_mail");
        String password = information.get("password");

        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                if (user.getPassword().equals(password)) {
                    answerJson = MetodsForAuthController.createAuthInformation(user, postsRepository); //Вызов метода отправки е-Mail

                    int sessionRandomInt = (int) (Math.random() * 1000);
                    session.setAttribute("name", user.getName() + sessionRandomInt);
                    sessionInformation.put((String) session.getAttribute("name"), user.getId());
                    return new Gson().toJson(answerJson);
                }
            }
        }
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    @GetMapping("/check")
    public String authCheckLogin(HttpServletRequest request) {


        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        if (sessionInformation.containsKey((String) session.getAttribute("name"))) {
            answerJson = MetodsForAuthController.createAuthInformation(usersRepository.findById(sessionInformation.get((String) session.getAttribute("name"))).get(), postsRepository);
            return new Gson().toJson(answerJson);
        }

        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    @GetMapping("/logout")
    public String authLogout(HttpServletRequest request) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        sessionInformation.remove((String) session.getAttribute("name"));
        answerJson.put("result", true);
        return new Gson().toJson(answerJson);
    }

    @PostMapping("/register")
    public String createNewUser(@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<Object, Object> errors = new HashMap<Object, Object>();
        int codeLength = 6;

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

        //Проверка соответствия captcha кода
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

    @GetMapping("/captcha")
    public String createCaptcha () throws IOException {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        int codeIndex = 0;
        int secretCodeIndex = 1;
        int captchaIndex = 2;
        int lifeTime = 1;

        //Проверка устаревания уже имеющихся кодов captcha
        captchaCodesRepository.findAll().forEach(captchaCodes -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -lifeTime);
            Date date = calendar.getTime();
            if (captchaCodes.getTime().before(date)) {
                captchaCodesRepository.delete(captchaCodes);
            }
        });

        ArrayList <String> createdCaptcha = MetodsForAuthController.metodsCreateCaptcha();
        CaptchaCodes captchaCodes = new CaptchaCodes();
        captchaCodes.setCode(createdCaptcha.get(codeIndex));
        captchaCodes.setSecretCode(createdCaptcha.get(secretCodeIndex));
        captchaCodes.setTime(new Date());
        captchaCodesRepository.save(captchaCodes);

        answerJson.put("secret", createdCaptcha.get(secretCodeIndex)); // Изменить на случайно генерируемый
        answerJson.put("image", createdCaptcha.get(captchaIndex));
        return new Gson().toJson(answerJson);
    }

    @PostMapping("/restore")
    public String restorePasswords (@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        String eMail = information.get("email");
        String restoreCode = MetodsForAuthController.generateRestoreCode();

        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                user.setCode(restoreCode);
                //Добавить метод отправки сообщений на емаил

                MetodsForAuthController.sendMail(eMail, restoreCode, supportMail, supportMailPassword);
                usersRepository.save(user);
                answerJson.put("result", true);
                return new Gson().toJson(answerJson);
            }
        }
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    @PostMapping("/password")
    public String createNewPassword (@RequestBody Map<String, String> information) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<Object, Object> errors = new HashMap<Object, Object>();
        int codeLength = 6;

        String code = information.get("code"); //На соответствие чему должен проверяться этот параметр?
        //Либо должна быть авторизация, либо Id Юзера, у которого этот код сохранен в БД
        String password = information.get("password");
        String captcha = information.get("captcha");
        String captchaSecret = information.get("captcha_secret");

        return "";
    }



    //============================================================================

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
