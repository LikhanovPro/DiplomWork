package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;

import main.controller.DefaultController;
import main.models.Posts;
import main.models.PostsVotes;
import main.models.Users;
import main.models.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GeneralGetMyStatistic {

    @JsonProperty
    int postsCount;

    @JsonProperty
    int likesCount;

    @JsonProperty
    int dislikesCount;

    @JsonProperty
    int viewsCount;

    @JsonProperty
    String firstPublication;

    public GeneralGetMyStatistic (HttpServletRequest request, UsersRepository usersRepository) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Integer userId = DefaultController.getIdUserLogin(request);//Получаем id пользователя
        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Ищем пользователя в БД
        Users user = usersRepository.findById(userId).get();
        List<Posts> posts = user.getUserPosts();//Получаем перечень постов авторизованного пользователя

        //Получем статистику авторизованного пользователя
        postsCount = posts.size();
        likesCount = 0;
        dislikesCount = 0;
        viewsCount = 0;
        Date date = new Date();
        for (Posts post : posts) {
            viewsCount += post.getViewCount();
            if (!date.before(post.getTime())) {
                date = post.getTime();
            }
            for (PostsVotes votes : post.getVotesToPost()) {
                if (votes.isValue()) {
                    likesCount++;
                }
                else {
                    dislikesCount++;
                }
            }
        }
        firstPublication = dateFormat.format(date);
    }
}
