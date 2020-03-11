package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@Controller
public class DefaultController {

    //Создаем объект, общий для всех контроллеров
    private static Map<String, Integer> sessionInformation = new HashMap<>();
    //======================================================

    @RequestMapping("/")
    public String index(Model model)
    {
        return "index";
    }

    @RequestMapping(method =
            {RequestMethod.OPTIONS, RequestMethod.GET}, //принимаем только GET OPTIONS
            value = "/**/{path:[^\\.]*}") //описание обрабатываемых ссылок (регулярка с переменной)
    public String redirectToIndex() {
        return "forward:/"; //делаем перенаправление
    }


    //=============================================================================================
    //Геттеры и Сеттеры, необходимые для реализации методов Класса
    public static Map<String, Integer> getSessionInformation() {
        return sessionInformation;
    }

    public static void setSessionInformation(Map<String, Integer> information) {
        sessionInformation = information;
    }

    public static Integer getIdUserLogin (HttpServletRequest request) {
        HttpSession session = request.getSession();

        return sessionInformation.get(String.valueOf(session.getAttribute("name")));
    }
}
