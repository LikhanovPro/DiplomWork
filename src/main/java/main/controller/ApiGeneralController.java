package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        int userId = ApiAuthController.getIdUserLogin(request);
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<String, String> errors = new HashMap<>();

        PostComments postComments = new PostComments();
        if (parentId != null){
            if (postCommentsRepository.findById(parentId).isPresent()) {
                postComments.setParentId(parentId);
            }
            else {
                answerJson.put("result", false);
                //Добавить BadRequest с ключем message, который будет выводиться в Alert
                return new Gson().toJson(answerJson);
            }
        }
        if (text.length() < 2 || text.isEmpty()) {
            answerJson.put("result", false);
            errors.put("text", "Текст комментария не задан или слишком короткий");
            //Добавить BadRequest с ключем message, который будет выводиться в Alert
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
                                  @RequestParam("post_id") int postId,
                                  @RequestParam("decision") String moderationStatus) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<String, String> errors = new HashMap<>();
        int userId = ApiAuthController.getIdUserLogin(request);
        if (usersRepository.findById(userId).get().isModerator()){
            ModeratorStatus status = ModeratorStatus.valueOf(moderationStatus);
            Posts post = postsRepository.findById(postId).get();
            post.setModerationStatus(status);
            post.setModeratorId(userId);
            postsRepository.save(post);
            answerJson.put("result", true);
            return new Gson().toJson(answerJson);
        }
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }
}


