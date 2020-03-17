package main.controller;

import main.requestObject.GeneralPostMProfileObject;
import main.requestObject.GeneralPostModerationObject;
import main.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
public class ApiGeneralController {

    @Autowired
    GeneralService generalService;

    //Передаем на frontend ощую информацию о создателе сайта
    @GetMapping ("/api/init")
    public ResponseEntity blogInformation () {
        return generalService.generalInit();
    }

    //передаем на frontend перечень Тэгов
    @GetMapping ("/api/tag")
    public ResponseEntity getTag () {
        return generalService.generalTags();
    }

    //Контроллер годов, в которые были публикации
    @GetMapping ("/api/calendar")
    public ResponseEntity getCalendar (@RequestParam("year") String year) {
        return generalService.generalGetCalendar(year);
    }

    //Контроллер создания коментария к посту или к коментарию
    @PostMapping ("/api/comment")
    public ResponseEntity addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {
       return generalService.addComment(request, parentId, postId, text);
    }

    //Контроллер модерации поста
    @PostMapping ("/api/moderation")
    public ResponseEntity moderationPost (HttpServletRequest request,
                                  @RequestBody GeneralPostModerationObject information){
        return generalService.generalModeration(request, information);
    }

    //Контроллер сохранения картинки
    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @PathVariable String image) {
        return generalService.generalImage(request, image);
    }

    //Контроллер вывода на экран статистики пользователя
    @GetMapping("/api/statistics/my")
    public ResponseEntity myStatistics (HttpServletRequest request) {
        return generalService.generalMyStatistic(request);
    }

    //Контролер вывода статистики по сайту в общем
    @GetMapping("/api/statistics/all")
    public ResponseEntity allStatistics (HttpServletRequest request) {
        return generalService.generalAllStatistic(request);
    }

    //Контроллер передачи настроек сайта
    @GetMapping("/api/settings")
    public ResponseEntity getSettings (HttpServletRequest request) {
        return generalService.generalGetSetting(request);
    }

    //Контроллер изменения настроек сайта
    @PutMapping ("/api/settings")
    public ResponseEntity saveSettings (HttpServletRequest request) {
        return generalService.generalPutSetting(request);
    }

    //Контроллер установки порфиля
    @PostMapping("api/profile/my")
    public ResponseEntity editProfile (HttpServletRequest request,
                               @RequestBody GeneralPostMProfileObject information) {
        return generalService.generalMyProfile(request, information);
    }
}


