package main.controller;

import main.Main;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class DefaultController {

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    @Autowired
    private GlobalSettingRepository globalSettingRepository;
    @Autowired
    private PostCommentsRepository postCommentsRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private PostsVotesRepository postsVotesRepository;
    @Autowired
    private Tag2PostRepository tag2PostRepository;
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private UsersRepository usersRepository;


    @RequestMapping("/")
    public String index(Model model)
    {
        /*Задаем глобальные настройки
        String multiUserCode = "MULTIUSER_MODE";
        String postPremoderationCode = "POST_PREMODERATION";
        String statisticIsPublicCode = "STATISTIC_IS_PUBLIC";

        String multiUserName = "Многопользовательский режим";
        String postPremoderationName = "Премодерация постов";
        String statisticIsPublicName = "Показать всем статистику блога";

        String multiUserValue = "YES";
        String postPremoderationValue = "YES";
        String statisticIsPublicValue = "YES";

        GlobalSetting multiUser = new GlobalSetting(1, multiUserCode, multiUserName, multiUserValue);
        GlobalSetting postPremoderation = new GlobalSetting(2, postPremoderationCode, postPremoderationName, postPremoderationValue);
        GlobalSetting statisticIsPublic = new GlobalSetting(3, statisticIsPublicCode, statisticIsPublicName, statisticIsPublicValue);

        globalSettingRepository.save(multiUser);
        globalSettingRepository.save(postPremoderation);
        globalSettingRepository.save(statisticIsPublic);*/

        return "index";
    }



}
