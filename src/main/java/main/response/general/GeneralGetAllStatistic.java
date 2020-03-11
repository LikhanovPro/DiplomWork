package main.response.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeneralGetAllStatistic {

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private GlobalSettingRepository globalSettingRepository;

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

    private GeneralGetAllStatistic (HttpServletRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Integer userId = null;
        userId = DefaultController.getIdUserLogin(request);

        int idStatisticsIsPublic = 3;//Номер id информации из БД, где указывает о доступе общей статистики неавторизованному пользователю

        //Проверка выполнения условий вывода информации по сайту
        if (!globalSettingRepository.findById(idStatisticsIsPublic).get().getValue() && userId.equals(null)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            List<Posts> posts = new ArrayList<>();
            //Заполнение списка общим список всех постов с сайта
            for (Posts post : postsRepository.findAll()) {
                posts.add(post);
            }
            //Сбор общей информации по всем постам
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
                    } else {
                        dislikesCount++;
                    }
                }
            }
            firstPublication = dateFormat.format(date);
        }
    }

    public GeneralGetAllStatistic () {}

    public ResponseEntity getAllStatistic (HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new GeneralGetAllStatistic(request));
    }
}
