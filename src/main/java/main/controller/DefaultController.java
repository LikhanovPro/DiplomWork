package main.controller;

import main.Main;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


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
