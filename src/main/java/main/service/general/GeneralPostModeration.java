package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.ModeratorStatus;
import main.models.Posts;
import main.models.PostsRepository;
import main.models.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class GeneralPostModeration {

    @JsonProperty
    boolean result;

    public GeneralPostModeration (HttpServletRequest request, Map<String, Object> information, PostsRepository postsRepository,
                                  UsersRepository usersRepository) {

        ModeratorStatus status;
        //Получаем id текущего пользователя
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Integer postId = (Integer) information.get("post_id");
        //решение модератора к посту
        String newStatus = (String) information.get("decision");

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
}
