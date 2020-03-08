package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.PostComments;
import main.models.PostCommentsRepository;
import main.models.PostsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GeneralPostComment {

    @JsonProperty
    boolean result;

    @JsonProperty
    int id;

    @JsonProperty
    Map<String, String> errors = new HashMap<>();

    public GeneralPostComment (HttpServletRequest request, Integer parentId, int postId, String text,
                               PostCommentsRepository postCommentsRepository, PostsRepository postsRepository) {
        int commentsLength = 2;//Минимальна длина коментария

        Integer userId = DefaultController.getIdUserLogin(request);
        //Проверка, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        PostComments postComments = new PostComments();
        //Проверка: коментарий к посту или к коментарию
        if (parentId != null){
            if (postCommentsRepository.findById(parentId).isPresent()) {
                postComments.setParentId(parentId);
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий коментарий не существует.");
            }
        }

        //Проверка существования поста, к которому передаются коментарии
        if (!postsRepository.findById(postId).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий пост не существует.");
        }

        //Проверка длины коментария
        if (text.length() < commentsLength || text.isEmpty()) {
            result = false;
            errors.put("text", "Текст комментария не задан или слишком короткий");
        }

        //Заполняем объект коментария информацией
        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setTime(new Date());
        postComments.setComment(text);

        result = true;
        //Сохраняем коментарий в БД, получая его id, который будет автоматически создан
        int postCommentsId = postCommentsRepository.save(postComments).getId();
        id = postCommentsId; //Заполняем json для возрата на frontend
    }
}
