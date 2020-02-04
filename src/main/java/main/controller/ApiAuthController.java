package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping ("/api/auth")
public class ApiAuthController extends HttpServlet {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    private static Map<String, Integer> sessionInformation = new HashMap<>();


    @PostMapping("/login")
    public String authLogin(@RequestBody Map<String, String> information, HttpServletRequest request) {
        String eMail = information.get("e_mail");
        String password = information.get("password");

        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        HttpSession session = request.getSession();

        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                if (user.getPassword().equals(password)) {
                    answerJson = MetodsForAuthController.createAuthInformation(user, postsRepository);

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
    public String createNewUser(@RequestParam("e_mail") String eMail,
                                @RequestParam("name") String name,
                                @RequestParam("password") String password,
                                @RequestParam("captcha") String captcha,
                                @RequestParam("captcha_secret") String captchaSecret) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<Object, Object> errors = new HashMap<Object, Object>();
        int codeLength = 6;

        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                answerJson.put("result", false);
                errors.put("email", "Этот e-mail уже зарегистрирован");
                answerJson.put("errors", errors);
                return new Gson().toJson(answerJson);
            }
        }
        if (password.length() < codeLength) {
            answerJson.put("result", false);
            errors.put("password", "Пароль короче 6-ти символов");
            answerJson.put("errors", errors);
            return new Gson().toJson(answerJson);
        }
        Users newUser = new Users();
        newUser.seteMail(captchaSecret);
        newUser.seteMail(eMail);
        newUser.setModerator(false);
        newUser.setName(name);
        newUser.setPassword(password);
        newUser.setRegTime(new Date());
        usersRepository.save(newUser);

        answerJson.put("result", true);
        return new Gson().toJson(answerJson);
    }
/*
    @GetMapping("/captcha")
    public String createCaptcha () {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        int code;
        answerJson.put("secret", "car4y8cryaw84cr89awnrc");
        answerJson.put("image", "картинка капчи в base64");
        return new Gson().toJson(answerJson);
    }*/



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
