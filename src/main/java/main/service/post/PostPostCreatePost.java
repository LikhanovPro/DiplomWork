package main.service.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostPostCreatePost {

    @JsonProperty
    boolean result;

    @JsonProperty
    Map <String, String> errors = new HashMap<>();


    public PostPostCreatePost (HttpServletRequest request, Map<String, Object> information, PostsRepository postsRepository, TagsRepository tagsRepository,
                               Tag2PostRepository tag2PostRepository) throws ParseException {

        int titleLength = 10; //Минимальное еколичество знаков заголовка
        int textLength = 500; //Минлнимальное количество знаков текста поста

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM");
        Integer userId = DefaultController.getIdUserLogin(request);
        Date time = dateFormat.parse(((String) information.get("time")).replaceAll("T", " "));//Дата передается со знаком "Т" ммежду датой и временем,убираю вручную
        boolean active;
        if (((Integer) information.get("active")) == 1){
            active = true;
        }
        else {
            active = false;
        }
        String title = (String) information.get("title");
        String text = (String) information.get("text");
        ArrayList<String> tags = (ArrayList<String>) information.get("tags");

        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверка соответствия даты реаьлности, не может быть будущее
        if (time.before(new Date())){
            time = new Date();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Задано прошедшее время.");
        }

        //Проверка соотвествия количества знаков заголовка и текста
        if (title.length() < titleLength || text.length() < textLength) {
            errors.put("title", "Заголовок не установлен");
            errors.put("text", "Текст публикации слишком короткий");
            result = false;
        }
        else {
            Posts post = new Posts();
            post.setActive(active);
            ModeratorStatus newPost;
            newPost = ModeratorStatus.NEW;
            post.setModerationStatus(newPost);
            post.setModeratorId(null);
            post.setText(text);
            post.setTime(time);
            post.setTitle(title);
            post.setUserId(userId);
            post.setViewCount(0);
            int postId = postsRepository.save(post).getId();

            //Поиск тегов, сохранение и получение их Id - при отладке вынести во внешний метод
            Map <String, Integer> tagsNames = new HashMap<>();
            for (Tags tag : tagsRepository.findAll()) {
                tagsNames.put(tag.getName(), tag.getId());
            }
            ArrayList <Integer> tagsId = new ArrayList<>();
            for (int i = 0; i < tags.size(); i++) {
                if (tagsNames.containsKey(tags.get(i).replaceAll(",", ""))) {
                    tagsId.add(tagsNames.get(tags.get(i)));
                }
                else {//Если тегов нет в БД, то создаем новый тег
                    Tags newTag = new Tags();
                    newTag.setName(tags.get(i));
                    tagsId.add(tagsRepository.save(newTag).getId());
                }
            }
            //Связываем теги с постом
            tagsId.forEach(tagId -> {
                Tag2Post tag2Post = new Tag2Post();
                tag2Post.setTagId(tagId);
                tag2Post.setPostId(postId);
                tag2PostRepository.save(tag2Post);
            });
            result = true;
        }
    }
}
