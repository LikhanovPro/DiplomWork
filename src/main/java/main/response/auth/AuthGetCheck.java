package main.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.Posts;
import main.models.PostsRepository;
import main.models.Users;
import main.models.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class AuthGetCheck {

    //Подключаем репозитории
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    @JsonProperty
    boolean result;
    @JsonProperty
    Map<Object, Object> user = new HashMap<>();

    private AuthGetCheck (HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, Integer> sessionInformation = DefaultController.getSessionInformation();

        this.result = false;
        //Проверка наличия сессии, т.е. есть ли авторизованный пользователь
        for (String key : sessionInformation.keySet()) {
            if (key.equals(String.valueOf(session.getAttribute("name")))) {
                Users user = usersRepository.findById(sessionInformation.get(key)).get();
                //Заполняем Json информацией о пользователе
                this.result = true;
                this.user.put("id", user.getId());
                this.user.put("name", user.getName());
                this.user.put("photo", user.getPhoto());
                this.user.put("email", user.geteMail());
                this.user.put("moderation", user.isModerator());

                int moderationCount = 0;
                //Считаем количество постов, которые были отмодерированы
                for (Posts post : postsRepository.findAll()) {
                    try {//Для новых публикаций модератор не определен, поэтому перехватываю исключение NullPointerException
                        if (post.getModeratorId() == user.getId()) {
                            moderationCount++;
                        }
                    } catch (NullPointerException ex){
                        System.out.println("Для этого поста модератор не определен");
                    }
                }
                this.user.put("moderationCount", moderationCount);
                this.user.put("settings", user.isModerator());
            }
        }
    }

    public ResponseEntity getAuthGetCheck (HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new AuthGetCheck(request));
    }

    public AuthGetCheck () {

    }
}
