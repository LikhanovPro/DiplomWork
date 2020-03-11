package main.controller;

import main.requestObject.auth.AuthPostLogInObject;
import main.requestObject.auth.AuthPostPasswordObject;
import main.requestObject.auth.AuthPostRegisterObject;
import main.requestObject.auth.AuthPostRestoreObject;
import main.response.auth.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController
@RequestMapping ("/api/auth")
public class ApiAuthController extends HttpServlet {

    private AuthPostLogIn authPostLogIn = new AuthPostLogIn();

    private AuthGetCheck authGetCheck = new AuthGetCheck();

    private AuthGetLogOut authGetLogOut = new AuthGetLogOut();

    private AuthPostRegister authPostRegister = new AuthPostRegister();

    private AuthGetCaptcha authGetCaptcha = new AuthGetCaptcha();

    private AuthPostRestore authPostRestore = new AuthPostRestore();

    private AuthPostPassword authPostPassword = new AuthPostPassword();

    /*//Подключаем репозитории
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    //======================================================*/

    //Задаем почту и пароль к eMail Support с которого будет рассылка почты
    private static String supportMail = "DiplomWorkSup@gmail.com";
    private static String supportMailPassword = "123DiplomWork";
    //=======================================================

    //Котроллер входа под своей учетной записью
    @PostMapping("/login")
    public ResponseEntity authLogin(@RequestBody AuthPostLogInObject information, HttpServletRequest request) {
        return authPostLogIn.getAuthPostLogIn(information, request);
    }

    //Контроллер првоерки текущего пользователя
    @GetMapping("/check")
    public ResponseEntity authCheckLogin(HttpServletRequest request) {
        return authGetCheck.getAuthGetCheck(request);
    }

    //Контроллер выхода из авторизации
    @GetMapping("/logout")
    public ResponseEntity authLogout(HttpServletRequest request) {
        return authGetLogOut.getAuthGetLogOut(request);
    }

    //Контроллер регистрации пользователя
    @PostMapping("/register")
    public ResponseEntity createNewUser(@RequestBody AuthPostRegisterObject information) {
        return authPostRegister.getAuthPostRegister(information);
    }

    //Создание captcha кода, запись его в БД
    @GetMapping("/captcha")
    public ResponseEntity createCaptcha () throws IOException {
        return authGetCaptcha.getCaptcha();
    }

    //Контроллер восстановления пароля
    @PostMapping("/restore")
    public ResponseEntity restorePasswords (@RequestBody AuthPostRestoreObject information) {
        return authPostRestore.getAuthPostRestore(information, supportMail, supportMailPassword);
    }

    //Контроллер изменения пароля (password) пользователя
    @PostMapping("/password")
    public ResponseEntity createNewPassword (@RequestBody AuthPostPasswordObject information) {
        return authPostPassword.getAuthPostPassword(information);
    }
}
