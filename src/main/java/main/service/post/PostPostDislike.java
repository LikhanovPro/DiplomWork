package main.service.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.PostsVotes;
import main.models.PostsVotesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

public class PostPostDislike {

    @JsonProperty
    boolean result;

    public PostPostDislike (HttpServletRequest request, Map<String, Object> information, PostsVotesRepository postsVotesRepository) {

        Integer userId = DefaultController.getIdUserLogin(request);
        Integer postId = (Integer) information.get("post_id");
        //Проверка авторизации польлзователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверяем была ли уже оценка данного поста данным пользователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {
                if (!vote.isValue()) {
                    result = false;//Если оценка была негативна, то ничего не делаем
                }
                else {//Если оценка была позитивна, то удаляем ее и ставим дизлайк
                    vote.setValue(false);
                    vote.setTime(new Date());
                    postsVotesRepository.save(vote);
                    result = true;
                }
            }
        }
        //Оценки поста данным пользователем не было, создаем новую оценку и связываем с постом
        PostsVotes vote = new PostsVotes();
        vote.setPostId(postId);
        vote.setUserId(userId);
        vote.setTime(new Date());
        vote.setValue(false);
        postsVotesRepository.save(vote);
        result = true;
    }
}
