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

public class PostGetMyPosts {

    @Autowired
    private PostsRepository postsRepository;

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList <Map> posts = new ArrayList<>();

    private PostGetMyPosts (HttpServletRequest request, int offset, int limit, String status) {

        ArrayList<Map> allPosts = new ArrayList<>();
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Выбчбор статуса постов
        switch (status) {
            case ("inactive") :
                postsRepository.findAll().forEach(post -> {
                    if (!post.isActive() && post.getUser().getId() == userId) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("pending") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("NEW")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("declined") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("DECLINED")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("published") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
        }
        count = allPosts.size();//Фиксируем количество
        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
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
    //Формирование информации о посте для личного списка постов
    private static Map <Object, Object> getMyPostInformation (Posts post) {
        Map<Object, Object> mapForAnswer = new HashMap<>();
        Map<Object, Object> userMap = new HashMap<>();
        int annoncelength = 100;

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());
        mapForAnswer.put("title", post.getTitle());

        String annonce;
        if (post.getText().length() < annoncelength) {
            annonce = post.getText();
        } else {
            annonce = post.getText().substring(0, annoncelength) + "...";
        }
        mapForAnswer.put("announce", annonce);

        //В описательной части API запросов задания нет требований к этой информации в ответе, но
        // при анализе работы веб-страницы это необходимо для работы сайта
        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        //===================================================
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()) {
                likeCount++;
            } else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", post.getCommentsToPost().size());
        mapForAnswer.put("viewCount", post.getViewCount());
        return mapForAnswer;
    }

    public PostGetMyPosts () {}

    public ResponseEntity getMyPost (HttpServletRequest request, int offset, int limit, String status) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostGetMyPosts(request,offset,limit, status));
    }
}
