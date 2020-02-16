package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ApiGeneralController {

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

    @GetMapping ("/api/init")
    public static String blogInformation () {
        Gson gson = new Gson();
        Map <String, String> map = new HashMap<>();
        map.put("title", "DevPub");
        map.put("subtitle", "Рассказы разработчиков");
        map.put("phone", "+7 903 666-44-55");
        map.put("email", "mail@mail.ru");
        map.put("copyright", "Дмитрий Сергеевич");
        map.put("copyrightFrom", "2005");
        String json = gson.toJson(map);
        return json;
    }

    /*@GetMapping ("/api/tag")
    public String getTag (@RequestParam("query") String query) {
        if (!query.equals(null)){
            return new Gson().toJson(MetodsForGeneralController.createMapForTagWithoutQuery(tagsRepository));
        }
        return new Gson().toJson(MetodsForGeneralController.createMapForTag(query, tagsRepository));
    }*/
    @GetMapping ("/api/tag")
    public String getTag () {
        Map <String, ArrayList> answerJson = new HashMap<>();
        answerJson.put("tags", MetodsForGeneralController.createMapForTagWithoutQuery(tagsRepository));
        return new Gson().toJson(answerJson);
    }

    @GetMapping ("/api/calendar")
    public String getCalendar (@RequestParam("year") String year) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

        if (year.length() != 4 || year.isEmpty()) {
            return new Gson().toJson(MetodsForGeneralController.createMapForGetCalendar( Integer.parseInt(dateFormat.format(new Date())), postsRepository));
        }
        else {
            return new Gson().toJson(MetodsForGeneralController.createMapForGetCalendar( Integer.parseInt(year), postsRepository));
        }
    }

    @PostMapping ("/api/comment")
    public String addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<String, String> errors = new HashMap<>();

        PostComments postComments = new PostComments();
        if (parentId != null){
            if (postCommentsRepository.findById(parentId).isPresent()) {
                postComments.setParentId(parentId);
            }
            else {
                //answerJson.put("result", false);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий коментарий не существует.");
                //return new Gson().toJson(answerJson);
            }
        }

        if (!postsRepository.findById(postId).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий пост не существует.");
        }


        if (text.length() < 2 || text.isEmpty()) {
            answerJson.put("result", false);
            errors.put("text", "Текст комментария не задан или слишком короткий");
            return new Gson().toJson(answerJson);
        }

        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setTime(new Date());
        postComments.setComment(text);
        int postCommentsId = postCommentsRepository.save(postComments).getId();
        answerJson.put("id", postCommentsId);
        return new Gson().toJson(answerJson);
    }

    @PostMapping ("/api/moderation")
    public String moderationPost (HttpServletRequest request,
                                  @RequestBody Map<String, Object> information){
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        ModeratorStatus status;
        Integer userId = ApiAuthController.getIdUserLogin(request);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Integer postId = (Integer) information.get("post_id");
        String newStatus = (String) information.get("decision");

        Posts post = postsRepository.findById(postId).get();

        if (usersRepository.findById(userId).get().isModerator()){
            if (newStatus.equals("accept")) {
                status = ModeratorStatus.valueOf("ACCEPTED");
                post.setModerationStatus(status);
            }
            if (newStatus.equals("decline")){
                status = ModeratorStatus.valueOf("DECLINED");
                post.setModerationStatus(status);
            }
            post.setModeratorId(userId);
            postsRepository.save(post);
            answerJson.put("result", true);
            return new Gson().toJson(answerJson);

        }
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @PathVariable String image) {
        Random random = new Random();
        Integer userId = ApiAuthController.getIdUserLogin(request);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        String userName = usersRepository.findById(userId).get().getName();
        StringBuilder pathToFolderWithImage = new StringBuilder();
        pathToFolderWithImage.append("src/main/resources/upload/");
        //Создадим дочерние подпапки из имени Юзера
        int maxLevelOfDirectory = 3;
        for (int i = 0; i < maxLevelOfDirectory; i ++) {
            pathToFolderWithImage.append(userName.charAt(random.nextInt(userName.length())));
            pathToFolderWithImage.append("/");
        }
        pathToFolderWithImage.append(image);
        File imageFile = new File(image);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(imageFile);
            ImageIO.write(bi, "png", new File(String.valueOf(pathToFolderWithImage)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathToFolderWithImage.toString();
    }

    @GetMapping("/api/statistics/my")
    public String myStatistics (HttpServletRequest request) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        Map <Object, Object> answerForJson = new HashMap<>();

        for (Users user : usersRepository.findAll()) {
            if (user.getId() == userId) {
                List <Posts> posts = user.getUserPosts();
                answerForJson = MetodsForGeneralController.postsStatistics(posts);
                break;
            }
        }
        return new Gson().toJson(answerForJson);
    }

    @GetMapping("/api/statistics/all")
    public String allStatistics (HttpServletRequest request) {
        Integer userId = null;
        userId = ApiAuthController.getIdUserLogin(request);
        Map <Object, Object> answerForJson = new HashMap<>();
        int idStatisticsIsPublic = 3;

        if (!globalSettingRepository.findById(idStatisticsIsPublic).get().getValue() && userId.equals(null)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            List <Posts> posts = new ArrayList<>();
            for (Posts post : postsRepository.findAll()) {
                posts.add(post);
            }
            answerForJson = MetodsForGeneralController.postsStatistics(posts);
            return new Gson().toJson(answerForJson);
        }
    }

    @GetMapping("/api/settings")
    public String getSettings (HttpServletRequest request) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        Map <Object, Object> answerForJson = new HashMap<>();

        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                for (GlobalSetting globalSetting : globalSettingRepository.findAll()) {
                    answerForJson.put(globalSetting.getCode(), globalSetting.getValue());
                }
            }
        }

        else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        return new Gson().toJson(answerForJson);
    }

    @PutMapping ("/api/settings")
    public String saveSettings (HttpServletRequest request) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        Map <Object, Object> answerForJson = new HashMap<>();

        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                for (GlobalSetting globalSetting : globalSettingRepository.findAll()) {
                    answerForJson.put(globalSetting.getCode(), globalSetting.getValue());
                }
            }
        }
        else {
              throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        return new Gson().toJson(answerForJson);
    }

    @PostMapping("api/profile/my")
    public String editProfile (HttpServletRequest request,
                               @RequestBody Map<String, Object> information) {

        Map <Object, Object> answerForJson = new HashMap<>();
        Integer userId = ApiAuthController.getIdUserLogin(request);

        String photo =(String) information.get("photo");
        boolean removePhoto = (boolean) information.get("removePhoto");
        String name = (String) information.get("name");
        String eMail = (String) information.get("email");
        String password = (String) information.get("password");

        if (userId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            Users user = usersRepository.findById(userId).get();
            if (!photo.isEmpty()) {
                user.setPhoto(photo);
            }
            if (removePhoto) {
                user.setPhoto(null);
            }
            user.setName(name);
            user.seteMail(eMail);
            user.setPassword(password);
            usersRepository.save(user);
            answerForJson.put("result", true);
        }
        return new Gson().toJson(answerForJson);
    }

}


