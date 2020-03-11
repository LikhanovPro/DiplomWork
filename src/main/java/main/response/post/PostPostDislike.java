package main.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.PostsVotes;
import main.models.PostsVotesRepository;
import main.requestObject.post.PostPostDislikeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

public class PostPostDislike {

    @Autowired
    private PostsVotesRepository postsVotesRepository;

    @JsonProperty
    boolean result;

    public PostPostDislike (HttpServletRequest request, PostPostDislikeObject information) {

        Integer userId = DefaultController.getIdUserLogin(request);
        Integer postId = information.getPostId();
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

    public PostPostDislike () {}

    public ResponseEntity getDislike (HttpServletRequest request, PostPostDislikeObject information) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostPostDislike(request, information));
    }
}
