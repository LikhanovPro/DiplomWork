package main.response.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.*;
import main.requestObject.general.GeneralPostModerationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class GeneralPostModeration {

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @JsonProperty
    boolean result;

    private GeneralPostModeration (HttpServletRequest request, GeneralPostModerationObject information) {

        ModeratorStatus status;
        //Получаем id текущего пользователя
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Integer postId = information.getPostId();
        //решение модератора к посту
        String newStatus = information.getNewStatus();

        Posts post = postsRepository.findById(postId).get();

        if (usersRepository.findById(userId).get().isModerator()){//Проверка, что авторизованный пользователь - модератор
            if (newStatus.equals("accept")) {//новый статус поста
                status = ModeratorStatus.valueOf("ACCEPTED");
                post.setModerationStatus(status);
            }
            if (newStatus.equals("decline")){//новый статус поста
                status = ModeratorStatus.valueOf("DECLINED");
                post.setModerationStatus(status);
            }
            post.setModeratorId(userId);//сохраняем id модераторра
            postsRepository.save(post);
            result = true;
        }
        result = false;
    }

    public GeneralPostModeration () {}

    public ResponseEntity moderationPost (HttpServletRequest request, GeneralPostModerationObject information) {
        return ResponseEntity.status(HttpStatus.OK).body(new GeneralPostModeration (request, information));
    }

}
