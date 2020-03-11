package main.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.Posts;
import main.models.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostGetPostsForModeration {

    @Autowired
    private PostsRepository postsRepository;

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList <Map> posts = new ArrayList<>();

    private PostGetPostsForModeration (HttpServletRequest request, int offset, int limit, String status) {

        ArrayList<Map> allPosts = new ArrayList<>();

        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверка постов соответствию условий
        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModerationStatus().toString().equals("NEW")) {//только новые посты
                allPosts.add(getPostInformationForModeration(post));
            }
            else {
                if (post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModeratorId() == userId) {//Посты соотвествующие статусу
                    allPosts.add(getPostInformationForModeration(post));
                }
            }
        });
        count = allPosts.size();//Фиксируем количество постов
        // Определяемся с количеством выводимого на экран
        // Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (allPosts.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                posts.add(allPosts.get(i));
            }
        }
        else {
            for (int i = offset; i < allPosts.size(); i++) {
                posts.add(allPosts.get(i));
            }
        }
    }
    private static Map <Object, Object> getPostInformationForModeration (Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();
        int annoncelength = 100;

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());

        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());

        String annonce;
        if (post.getText().length() < annoncelength) {
            annonce = post.getText();
        }
        else {
            annonce = post.getText().substring(0, annoncelength) + "...";
        }
        mapForAnswer.put("announce", annonce);

        return mapForAnswer;
    }

    public PostGetPostsForModeration () {}

    public ResponseEntity moderationPost (HttpServletRequest request, int offset, int limit, String status) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostGetPostsForModeration(request, offset, limit, status));
    }
}
