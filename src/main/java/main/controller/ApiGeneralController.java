package main.controller;

import main.models.*;
import main.service.general.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class ApiGeneralController {

    //Подключаем репозитории
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
    //=====================================================

    //Передаем на frontend ощую информацию о создателе сайта
    @GetMapping ("/api/init")
    public static ResponseEntity blogInformation () {

        GeneralGetInit newGeneralInit = new GeneralGetInit("DevPub", "Рассказы разработчиков", "+7 903 666-44-55",
                "mail@mail.ru", "Дмитрий Сергеевич", "2005");

        return ResponseEntity.status(HttpStatus.OK).body(newGeneralInit);
    }

    //передаем на frontend перечень Тэгов
    @GetMapping ("/api/tag")
    public ResponseEntity getTag () {

        GeneralGetTag tagList = new GeneralGetTag(tagsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(tagList);
    }

    //Контроллер годов, в которые были публикации
    @GetMapping ("/api/calendar")
    public ResponseEntity getCalendar (@RequestParam("year") String year) {

        GeneralGetCalendar calendar = new GeneralGetCalendar(year, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(calendar);
    }

    //Контроллер создания коментария к посту или к коментарию
    @PostMapping ("/api/comment")
    public ResponseEntity addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {

        GeneralPostComment comment = new GeneralPostComment(request, parentId, postId, text, postCommentsRepository, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(comment);
    }

    //Контроллер модерации поста
    @PostMapping ("/api/moderation")
    public ResponseEntity moderationPost (HttpServletRequest request,
                                  @RequestBody Map<String, Object> information){

        GeneralPostModeration moderation = new GeneralPostModeration(request, information, postsRepository, usersRepository);

        return ResponseEntity.status(HttpStatus.OK).body(moderation);
    }

    //Контроллер сохранения картинки
    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @PathVariable String image) {

        GeneralPostImage copyImage = new GeneralPostImage(request, image, usersRepository);

        return copyImage.getPathToImage();
    }

    //Контроллер вывода на экран статистики пользователя
    @GetMapping("/api/statistics/my")
    public ResponseEntity myStatistics (HttpServletRequest request) {

        GeneralGetMyStatistic getMyStatistic = new GeneralGetMyStatistic(request, usersRepository);

        return ResponseEntity.status(HttpStatus.OK).body(getMyStatistic);
    }

    //Контролер вывода статистики по сайту в общем
    @GetMapping("/api/statistics/all")
    public ResponseEntity allStatistics (HttpServletRequest request) {

        GeneralGetAllStatistic getAllStatistic = new GeneralGetAllStatistic(request, globalSettingRepository, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(getAllStatistic);
    }

    //Контроллер передачи настроек сайта
    @GetMapping("/api/settings")
    public ResponseEntity getSettings (HttpServletRequest request) {

        GeneralGetSetting getSetting = new GeneralGetSetting(request, usersRepository, globalSettingRepository);

        return ResponseEntity.status(HttpStatus.OK).body(getSetting);
    }

    //Контроллер изменения настроек сайта
    @PutMapping ("/api/settings")
    public ResponseEntity saveSettings (HttpServletRequest request) {

        GeneralPutSetting putSetting = new GeneralPutSetting(request, usersRepository, globalSettingRepository);

        return ResponseEntity.status(HttpStatus.OK).body(putSetting);
    }

    //Контроллер установки порфиля
    @PostMapping("api/profile/my")
    public ResponseEntity editProfile (HttpServletRequest request,
                               @RequestBody Map<String, Object> information) {

        GeneralPostMyProfile myProfile = new GeneralPostMyProfile(request, information, usersRepository);

        return ResponseEntity.status(HttpStatus.OK).body(myProfile);
    }

}


