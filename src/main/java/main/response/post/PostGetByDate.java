package main.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.models.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostGetByDate {

    @Autowired
    private PostsRepository postsRepository;

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList <Map> posts = new ArrayList<>();

    private PostGetByDate (int offset, int limit, String date) {

        ArrayList<Map> allPosts = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //Проверяем все посты на соответствие условиям
        postsRepository.findAll().forEach(post -> {
            //Проверка постов указанной дате, которая была передана с frontend
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") &&
                    dateFormat.format(post.getTime()).equals(date)) {
                allPosts.add(getPostInformation(post.getId(), postsRepository));//Заполнение информации по посту
            }
        });

        count = allPosts.size();//Фиксация количества постов
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

    //Метод формирования информации о постах
    public static Map <Object, Object> getPostInformation (Integer postId, PostsRepository postsRepository) {
        int annoncelength = 100;//Количество знаков в анонсе

        Map<Object, Object> mapForAnswer = new HashMap<>();
        Map<Object, Object> userMap = new HashMap<>();

        //Заполнение информации
        mapForAnswer.put("id", postId);
        mapForAnswer.put("time", postsRepository.findById(postId).get().getTime().toString());
        userMap.put("id", postsRepository.findById(postId).get().getUser().getId());
        userMap.put("name", postsRepository.findById(postId).get().getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", postsRepository.findById(postId).get().getTitle());

        String annonce;
        //Формирование анонса
        if (postsRepository.findById(postId).get().getText().length() < annoncelength) {//Проверка, что длина текста больше максимальной длины анонса
            annonce = postsRepository.findById(postId).get().getText();
        } else {
            annonce = postsRepository.findById(postId).get().getText().substring(0, annoncelength) + "...";//Добавление троеточия в конце анонса
        }

        mapForAnswer.put("announce", annonce);

        int likeCount = 0;
        int dislikeCounts = 0;
        //Подсчет количества лайков и дизлайков
        for (int i = 0; i < postsRepository.findById(postId).get().getVotesToPost().size(); i++) {
            if (postsRepository.findById(postId).get().getVotesToPost().get(i).isValue()) {
                likeCount++;
            } else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", postsRepository.findById(postId).get().getCommentsToPost().size());
        mapForAnswer.put("viewCount", postsRepository.findById(postId).get().getViewCount());

        return mapForAnswer;
    }

    public PostGetByDate () {}

    public ResponseEntity getPost (int offset, int limit, String date) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostGetByDate(offset, limit, date));
    }
}
