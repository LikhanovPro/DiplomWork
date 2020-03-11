package main.controller;

import main.models.*;
import main.requestObject.general.GeneralPostMProfileObject;
import main.requestObject.general.GeneralPostModerationObject;
import main.response.general.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class ApiGeneralController {

    private GeneralGetInit generalGetInit = new GeneralGetInit();

    private GeneralGetTag generalGetTag = new GeneralGetTag();

    private GeneralGetCalendar generalGetCalendar = new GeneralGetCalendar();

    private GeneralPostComment generalPostComment = new GeneralPostComment();

    private GeneralPostModeration generalPostModeration = new GeneralPostModeration();

    private GeneralPostImage generalPostImage = new GeneralPostImage();

    private GeneralGetMyStatistic generalGetMyStatistic = new GeneralGetMyStatistic();

    private GeneralGetAllStatistic generalGetAllStatistic = new GeneralGetAllStatistic();

    private GeneralGetSetting generalGetSetting = new GeneralGetSetting();

    private GeneralPutSetting generalPutSetting = new GeneralPutSetting();

    private GeneralPostMyProfile generalPostMyProfile = new GeneralPostMyProfile();

    /*//Подключаем репозитории
    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostCommentsRepository postCommentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GlobalSettingRepository globalSettingRepository;
    //=====================================================*/

    //Передаем на frontend ощую информацию о создателе сайта
    @GetMapping ("/api/init")
    public ResponseEntity blogInformation () {
        return generalGetInit.getGeneralInit("DevPub", "Рассказы разработчиков", "+7 903 666-44-55",
                "mail@mail.ru", "Дмитрий Сергеевич", "2005");
    }

    //передаем на frontend перечень Тэгов
    @GetMapping ("/api/tag")
    public ResponseEntity getTag () {
        return generalGetTag.getTags();
    }

    //Контроллер годов, в которые были публикации
    @GetMapping ("/api/calendar")
    public ResponseEntity getCalendar (@RequestParam("year") String year) {
        return generalGetCalendar.getCalendar(year);
    }

    //Контроллер создания коментария к посту или к коментарию
    @PostMapping ("/api/comment")
    public ResponseEntity addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {
       return generalPostComment.addComments(request, parentId, postId, text);
    }

    //Контроллер модерации поста
    @PostMapping ("/api/moderation")
    public ResponseEntity moderationPost (HttpServletRequest request,
                                  @RequestBody GeneralPostModerationObject information){
        return generalPostModeration.moderationPost(request, information);
    }

    //Контроллер сохранения картинки
    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @PathVariable String image) {
        return generalPostImage.getPathToImage(request, image);
    }

    //Контроллер вывода на экран статистики пользователя
    @GetMapping("/api/statistics/my")
    public ResponseEntity myStatistics (HttpServletRequest request) {
        return generalGetMyStatistic.getMyStatistic(request);
    }

    //Контролер вывода статистики по сайту в общем
    @GetMapping("/api/statistics/all")
    public ResponseEntity allStatistics (HttpServletRequest request) {
        return generalGetAllStatistic.getAllStatistic(request);
    }

    //Контроллер передачи настроек сайта
    @GetMapping("/api/settings")
    public ResponseEntity getSettings (HttpServletRequest request) {
        return generalGetSetting.getSetting(request);
    }

    //Контроллер изменения настроек сайта
    @PutMapping ("/api/settings")
    public ResponseEntity saveSettings (HttpServletRequest request) {
        return generalPutSetting.putSetting(request);
    }

    //Контроллер установки порфиля
    @PostMapping("api/profile/my")
    public ResponseEntity editProfile (HttpServletRequest request,
                               @RequestBody GeneralPostMProfileObject information) {
        return generalPostMyProfile.changeMyProfile(request, information);
    }
}


