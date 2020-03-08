package main.controller;

import com.google.gson.Gson;
import main.models.*;
import main.service.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    //Создаем объект, общий для всех контроллеров
    //private static Map<String, Integer> sessionInformation = new HashMap<>();
    //======================================================

    //Задаем почту и пароль к eMail Support с которого будет рассылка почты
    private static String supportMail = "DiplomWorkSup@gmail.com";
    private static String supportMailPassword = "123DiplomWork";
    //=======================================================

    //Котроллер входа под своей учетной записью
    @PostMapping("/login")
    public ResponseEntity authLogin(@RequestBody Map<String, String> information, HttpServletRequest request) {

        AuthPostLogIn authLogin = new AuthPostLogIn();
        authLogin.getAuthInformation(information, usersRepository, postsRepository, request);
        return ResponseEntity.status(HttpStatus.OK).body(authLogin);
    }

    //Контроллер првоерки текущего пользователя
    @GetMapping("/check")
    public ResponseEntity authCheckLogin(HttpServletRequest request) {
        HttpSession session = request.getSession();

        AuthGetCheck authCheck = new AuthGetCheck();
        authCheck.checkAuthInformation(session, DefaultController.getSessionInformation(), usersRepository, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(authCheck);
    }

    //Контроллер выхода из авторизации
    @GetMapping("/logout")
    public ResponseEntity authLogout(HttpServletRequest request) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();
        // Из текущей сессии получаем id авторизованного пользователя и удаляем его из сессии
        DefaultController.getSessionInformation().remove(String.valueOf(session.getAttribute("name")));
        answerJson.put("result", true);
        return ResponseEntity.status(HttpStatus.OK).body(new Gson().toJson(answerJson));
    }

    //Контроллер регистрации пользователя
    @PostMapping("/register")
    public ResponseEntity createNewUser(@RequestBody Map<String, String> information) {
        int codeLength = 6; //Минлимальная длина пароля (password)

        AuthPostRegister authPostRegister = new AuthPostRegister();
        authPostRegister.authPostRegister(information,usersRepository, postsRepository, captchaCodesRepository, codeLength);

        return ResponseEntity.status(HttpStatus.OK).body(authPostRegister);
    }

    //Создание captcha кода, запись его в БД
    @GetMapping("/captcha")
    public ResponseEntity createCaptcha () throws IOException {

        AuthGetCaptcha captcha = new AuthGetCaptcha();
        captcha.createCaptcha(captchaCodesRepository);

        return ResponseEntity.status(HttpStatus.OK).body(captcha);
    }

    //Контроллер восстановления пароля
    @PostMapping("/restore")
    public ResponseEntity restorePasswords (@RequestBody Map<String, String> information) {

        AuthPostRestore restore = new AuthPostRestore();
        restore.restorePassword(information, supportMail, supportMailPassword, usersRepository);

        return ResponseEntity.status(HttpStatus.OK).body(restore);
    }

    //Контроллер изменения пароля (password) пользователя
    @PostMapping("/password")
    public ResponseEntity createNewPassword (@RequestBody Map<String, String> information) {

        AuthPostPassword newPassword = new AuthPostPassword();
        newPassword.createPassword(information, captchaCodesRepository, usersRepository);

        return ResponseEntity.status(HttpStatus.OK).body(newPassword);
    }
}
