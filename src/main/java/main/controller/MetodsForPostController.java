package main.controller;

import main.models.Posts;
import main.models.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class MetodsForPostController {

    //Метод формирования информации о постах
    public static Map <Object, Object> createJsonForPostsList (Integer postId, PostsRepository postsRepository) {
        int annoncelength = 100;//Количество знаков в анонсе

        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

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
        }
        else {
            annonce = postsRepository.findById(postId).get().getText().substring(0, annoncelength) + "...";//Добавление троеточия в конце анонса
        }

        mapForAnswer.put("announce", annonce);

        int likeCount = 0;
        int dislikeCounts = 0;
        //Подсчет количества лайков и дизлайков
        for (int i = 0; i < postsRepository.findById(postId).get().getVotesToPost().size(); i++) {
            if (postsRepository.findById(postId).get().getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", postsRepository.findById(postId).get().getCommentsToPost().size());
        mapForAnswer.put("viewCount", postsRepository.findById(postId).get().getViewCount());

        return mapForAnswer;
    }

    //Формирование информации о посте
    public static Map <Object, Object> createJsonForPostById(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

        ArrayList <Map> commentsList = new ArrayList<>();

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());

        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());
        mapForAnswer.put("text", post.getText());
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);


        post.getCommentsToPost().forEach(comment ->{
            Map <Object, Object> userComments = new HashMap<>();
            Map <Object, Object> commentsMap = new HashMap<>();
            commentsMap.put("id", comment.getId());
            commentsMap.put("time", comment.getTime());

            userComments.put("id", comment.getUserForComments().getId());
            userComments.put("name", comment.getUserForComments().getName());
            userComments.put("photo", comment.getUserForComments().getPhoto());
            commentsMap.put("user", userComments);
            commentsMap.put("text", comment.getComment());
            commentsList.add(commentsMap);
        });
        mapForAnswer.put("comments", commentsList);
        List <String> tags = new ArrayList<>();
        post.getTagsToPost().forEach(tag -> {
            tags.add(tag.getName());
        });
        mapForAnswer.put("tags", tags);
        return mapForAnswer;
    }

    //Формирование информации о посте при модерации
    public static Map <Object, Object> createJsonForModerationPosts(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();
        int annoncelength = 100;

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());

        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());

        String annonce;
        if (post.getText().length() < annoncelength) {
            annonce = post.getText();
        }
        else {
            annonce = post.getText().substring(0, annoncelength) + "...";
        }
        mapForAnswer.put("announce", annonce);

        return mapForAnswer;
    }

    //Формирование информации о посте для личного списка постов
    public static Map <Object, Object> createJsonForMyPosts(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();
        int annoncelength = 100;

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());
        mapForAnswer.put("title", post.getTitle());

        String annonce;
        if (post.getText().length() < annoncelength) {
            annonce = post.getText();
        }
        else {
            annonce = post.getText().substring(0, annoncelength) + "...";
        }
        mapForAnswer.put("announce", annonce);

        //В описательной части API запросов задания нет требований к этой информации в ответе, но
        // при анализе работы веб-страницы это необходимо для работы сайта
        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        //===================================================
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", post.getCommentsToPost().size());
        mapForAnswer.put("viewCount", post.getViewCount());
        return mapForAnswer;
    }

}
