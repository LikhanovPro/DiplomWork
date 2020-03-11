package main.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.PostsRepository;
import main.models.PostsVotes;
import main.models.PostsVotesRepository;
import main.requestObject.post.PostPostLikeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

public class PostPostLike {

    @Autowired
    private PostsVotesRepository postsVotesRepository;

    @JsonProperty
    boolean result;

    private PostPostLike (HttpServletRequest request, PostPostLikeObject information) {

        Integer userId = DefaultController.getIdUserLogin(request);
        Integer postId = information.getPostId();
        //Проверка авторизации польлзователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверяем была ли уже оценка данного поста данным пользователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {//оценка была
                if (vote.isValue()) {
                    result = false;//Если оценка была позитивна, то ничего не делаем
                }
                else {//Если оценка была негативна, то удаляем ее и ставим лайк
                    vote.setValue(true);
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
        vote.setValue(true);
        postsVotesRepository.save(vote);
        result = true;
    }

    public PostPostLike () {}

    public ResponseEntity getLike (HttpServletRequest request, PostPostLikeObject information) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostPostLike(request, information));
    }
}