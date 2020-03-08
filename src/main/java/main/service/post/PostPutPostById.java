package main.service.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostPutPostById {

    @JsonProperty
    boolean result;

    @JsonProperty
    Map<String, String> errors = new HashMap<>();

    public PostPutPostById (HttpServletRequest request, Date time, boolean active, String title, String text, String tags, int id,
                            UsersRepository usersRepository, PostsRepository postsRepository, TagsRepository tagsRepository, Tag2PostRepository tag2PostRepository) {

        int titleLength = 10; //Минимальное еколичество знаков заголовка
        int textLength = 500; //Минлнимальное количество знаков текста поста

        Integer userId = DefaultController.getIdUserLogin(request);
        //Проверяем авторизацию пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверка соответствия условий заголовка и текста
        if (title.length() < titleLength || text.length() < textLength) {
            errors.put("title", "Заголовок не установлен");
            errors.put("text", "Текст публикации слишком короткий");
            result = false;
        } else {
            String[] tagsList;
            tags.replaceAll(", ", ",");
            tagsList = tags.split(",");
            //Првоерка соответствия времени
            if (time.before(new Date())) {
                time = new Date();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Задано прошедшее время.");
            }

            //Получаем пост из БД по id переданному из frontend
            Posts post = postsRepository.findById(id).get();
            post.setActive(active);
            //Если пользователь модератор - то устанавливаем модератора посту, если нет, ставим посту статус NEW и ждм решения модератора
            if (usersRepository.findById(userId).get().isModerator()) {
                post.setModeratorId(userId);
            } else {
                ModeratorStatus newPost;
                newPost = ModeratorStatus.NEW;
                post.setModerationStatus(newPost);
            }
            post.setText(text);
            post.setTime(time);
            post.setTitle(title);
            int postId = postsRepository.save(post).getId();

            //Поиск тегов, сохранение и получение их Id - при отладке вынести во внешний метод
            Map<String, Integer> tagsNames = new HashMap<>();
            for (Tags tag : tagsRepository.findAll()) {
                tagsNames.put(tag.getName(), tag.getId());
            }
            //Проверка тегов на их наличие в БД, если нет - создание новых
            ArrayList<Integer> tagsId = new ArrayList<>();
            for (int i = 0; i < tagsList.length; i++) {
                if (tagsNames.containsKey(tagsList[i].replaceAll(",", ""))) {
                    tagsId.add(tagsNames.get(tagsList[i]));
                } else {
                    Tags newTag = new Tags();
                    newTag.setName(tagsList[i]);
                    tagsId.add(tagsRepository.save(newTag).getId());
                }
            }
            //Созжание связи между тегами и постом
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
