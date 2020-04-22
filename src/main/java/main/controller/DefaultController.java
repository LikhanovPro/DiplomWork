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
}
