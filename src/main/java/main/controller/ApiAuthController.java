package main.controller;

import main.requestObject.AuthPostLogInObject;
import main.requestObject.AuthPostPasswordObject;
import main.requestObject.AuthPostRegisterObject;
import main.requestObject.AuthPostRestoreObject;
import main.responseObject.*;
import main.service.AuthService;
import main.service.ResponseApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController
@RequestMapping ("/api/auth")
public class ApiAuthController extends HttpServlet {

    @Autowired
    AuthService authService;



    //Котроллер входа под своей учетной записью
    @PostMapping("/login")
    public ResponseEntity <ResponseApi> authLogin(@RequestBody AuthPostLogInObject information, HttpServletRequest request) {
        return authService.authLogIn(information, request);
    }

    //Контроллер првоерки текущего пользователя
    @GetMapping("/check")
    public ResponseEntity<ResponseApi> authCheckLogin(HttpServletRequest request) {
        return authService.authCheck(request);
    }

    //Контроллер выхода из авторизации
    @GetMapping("/logout")
    public ResponseEntity<ResponseApi> authLogout(HttpServletRequest request) {
        return authService.authLogOut(request);
    }

    //Контроллер регистрации пользователя
    @PostMapping("/register")
    public ResponseEntity<ResponseApi> createNewUser(@RequestBody AuthPostRegisterObject information) {
        return authService.authRegister(information);
    }

    //Создание captcha кода, запись его в БД
    @GetMapping("/captcha")
    public ResponseEntity<ResponseApi> createCaptcha () throws IOException {
        return authService.createCaptcha();
    }

    //Контроллер восстановления пароля
    @PostMapping("/restore")
    public ResponseEntity<ResponseApi> restorePasswords (@RequestBody AuthPostRestoreObject information) {
        return authService.authRestorePassword(information);
    }

    //Контроллер изменения пароля (password) пользователя
    @PostMapping("/password")
    public ResponseEntity<ResponseApi> createNewPassword (@RequestBody AuthPostPasswordObject information) {
        return authService.authChagePassword(information);
    }
}
