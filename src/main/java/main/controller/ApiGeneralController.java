package main.controller;

import main.requestObject.GeneralPostMProfileObject;
import main.requestObject.GeneralPostModerationObject;
import main.service.GeneralService;
import main.responseObject.ResponseApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class ApiGeneralController {

    @Autowired
    GeneralService generalService;

    //Передаем на frontend ощую информацию о создателе сайта
    @GetMapping ("/api/init")
    public ResponseEntity<ResponseApi> blogInformation () {
        return generalService.generalInit();
    }

    //передаем на frontend перечень Тэгов
    @GetMapping ("/api/tag")
    public ResponseEntity<ResponseApi> getTag () {
        return generalService.generalTags();
    }

    //Контроллер годов, в которые были публикации
    @GetMapping ("/api/calendar")
    public ResponseEntity<ResponseApi> getCalendar (@RequestParam("year") String year) {
        return generalService.generalGetCalendar(year);
    }

    //Контроллер создания коментария к посту или к коментарию
    @PostMapping ("/api/comment")
    public ResponseEntity<ResponseApi> addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {
       return generalService.addComment(request, parentId, postId, text);
    }

    //Контроллер модерации поста
    @PostMapping ("/api/moderation")
    public ResponseEntity<ResponseApi> moderationPost (HttpServletRequest request,
                                  @RequestBody GeneralPostModerationObject information){
        return generalService.generalModeration(request, information);
    }

    //Контроллер сохранения картинки
    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @RequestParam MultipartFile image) {
        return generalService.generalImage(request, image);
    }

    //Контроллер вывода на экран статистики пользователя
    @GetMapping("/api/statistics/my")
    public ResponseEntity<ResponseApi> myStatistics (HttpServletRequest request) {
        return generalService.generalMyStatistic(request);
    }

    //Контролер вывода статистики по сайту в общем
    @GetMapping("/api/statistics/all")
    public ResponseEntity <ResponseApi> allStatistics (HttpServletRequest request) {
        return generalService.generalAllStatistic(request);
    }

    //Контроллер передачи настроек сайта
    @GetMapping("/api/settings")
    public ResponseEntity<ResponseApi> getSettings (HttpServletRequest request) {
        return generalService.generalGetSetting(request);
    }

    //Контроллер изменения настроек сайта
    @PutMapping ("/api/settings")
    public ResponseEntity<ResponseApi> saveSettings (HttpServletRequest request) {
        return generalService.generalPutSetting(request);
    }

    @RequestMapping (value = "api/profile/my", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<ResponseApi> editProfileWithoutImage (HttpServletRequest request, @RequestBody GeneralPostMProfileObject info) {

        return generalService.generalMyProfileWithoutAvatar(request, info);
    }

    @RequestMapping (value = "api/profile/my", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseApi> editProfileWithImage (HttpServletRequest request,
                                                             MultipartFile photo, String removePhoto, String name, String email, String password) {
        return generalService.generalMyProfileWithAvatar(request, photo, removePhoto, name, email, password);
    }

    @GetMapping ("posts/{avatarImage}")
    public ResponseEntity<byte[]> getUserAvatar (HttpServletRequest request, @PathVariable String avatarImage) throws IOException {

        return generalService.getUserAvatar(request, avatarImage);
    }

    @GetMapping ("{avatarImage}")
    public ResponseEntity<byte[]> getUserAvatarForProfile (HttpServletRequest request, @PathVariable String avatarImage) throws IOException {

        return generalService.getUserAvatar(request, avatarImage);
    }
}


