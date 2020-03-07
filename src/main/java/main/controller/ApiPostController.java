package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping ("/api")
public class ApiPostController extends HttpServlet {

    //Подключаем репозитории
    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    @Autowired
    private GlobalSettingRepository globalSettingRepository;
    @Autowired
    private PostCommentsRepository postCommentsRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private PostsVotesRepository postsVotesRepository;
    @Autowired
    private Tag2PostRepository tag2PostRepository;
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private UsersRepository usersRepository;
    //==========================================

    @GetMapping ("/post")
    public String postsList (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("mode") String mode) {

        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();

        //Выбор метода отображения списка постов
        switch (mode) {
            case ("recent") :
                //Создаем список id постов с датами публикации
                Map <Integer, Date> idPostsListRecently = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListRecently.put(post.getId(), post.getTime());
                    }
                });
                answerJson.put("count", idPostsListRecently.size()); // Фиксируем количество постов
                idPostsListRecently.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue().reversed())//Выполняем сортировку по датам
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));//в порядке очереди заполняем информацию о постах
                break;
            case ("popular") :
                //Создаем список id постов с количеством коментариев
                Map <Integer, Integer> idPostsListPopular = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListPopular.put(post.getId(), post.getCommentsToPost().size());
                    }
                });
                answerJson.put("count", idPostsListPopular.size());//Фиксируем количество постов
                idPostsListPopular.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())//Выполняем сортировку по количеству коментариев
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));//в порядке очереди заполняем информацию о постах
                break;
            case ("best") :
                //Создаем список id постов с количеством лайков
                Map <Integer, Integer> idPostsListBest = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {

                        //Считаем количество лайков для постов
                        int likeCount = 0;
                        for (int i = 0; i < post.getVotesToPost().size(); i++) {
                            if (post.getVotesToPost().get(i).isValue()){
                                likeCount++;
                            }
                        idPostsListBest.put(post.getId(), likeCount);//Заполняем список
                    }
                }
                });
                answerJson.put("count", idPostsListBest.size());//Фиксируем количество постов
                idPostsListBest.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue())//Выполняем сортировку
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));//Заполняем в порядке очереди информацию о остах
                break;
            case ("early") :
                //Создаем список id постов с датами
                Map <Integer, Date> idPostsListEarly = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListEarly.put(post.getId(), post.getTime());
                    }
                });
                answerJson.put("count", idPostsListEarly.size());//Фиксируем количество постов
                idPostsListEarly.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue())//Сортируем по датам
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));//Заполняем по очереди информацию о постах
                break;
            default:
                answerJson.put("count", 0);
                break;
        }

        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForPostController.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForPostController.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер поиска постов по словам
    @GetMapping ("/post/search")
    public String searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();

        //Проверяем, что строка поиска не пуста
        if (!query.equals(null)) {
            postsRepository.findAll().forEach(post -> {
                //Сложный поиск по условиям:
                //1. Посты активны, и утверждены модератором
                //2. текст из строки поиска найден в тексте или заголовке поста
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&&
                        (post.getTitle().toUpperCase().contains(query.toUpperCase())||post.getText().toUpperCase().contains(query.toUpperCase()))) {
                    arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                }
            });
        }
        else {//Если пуста, то выводим все посты
            postsRepository.findAll().forEach(post -> {
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                    arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                }
            });

        }
        answerJson.put("count", arrayMapFormMetodsForPostController.size());//Фиксируем количество постов
        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForPostController.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForPostController.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер вывод поста по id
    @GetMapping ("/post/{id}")
    public String postById (@PathVariable int id) {
        Map <Object, Object> answerJson = new HashMap<Object, Object>();

        //Получаем пост по id номеру
        Posts post = postsRepository.findById(id).get();
        //Сложный запрос на соответствие поста условиям
        if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") && post.getTime().before(new Date())) {
            answerJson = MetodsForPostController.createJsonForPostById(post);
        }
        return new Gson().toJson(answerJson);
    }

    //Контроллер получения постов по дате публикации
    @GetMapping ("/post/byDate")
    public String postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //Проверяем все посты на соответствие условиям
        postsRepository.findAll().forEach(post -> {
            //Проверка постов указанной дате, которая была передана с frontend
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") &&
            dateFormat.format(post.getTime()).equals(date)) {
                arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));//Заполнение информации по посту
            }
        });

        answerJson.put("count", arrayMapFormMetodsForPostController.size());//Фиксация количества постов
        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForPostController.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForPostController.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер вывода постов по тегам
    @GetMapping ("/post/byTag")
    public String postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();

        //Проверяем все посты
        postsRepository.findAll().forEach(post -> {
            //Проверка условий поста
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                post.getTagsToPost().forEach(tagToPost -> {
                    if (tagToPost.getName().equals(tag)) {//Проверка соответствия тегу
                        arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                    }
                });
            }
        });
        answerJson.put("count", arrayMapFormMetodsForPostController.size());//Фиксируем количество
        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForPostController.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForPostController.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForPostController.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер вывода постов для модерации
    @GetMapping ("/post/moderation")
    public String moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit,
                                   @RequestParam ("status") String status)
                                   {
        ArrayList <Map> arrayMapFormMetodsForModerationPosts = new ArrayList<>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();

        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверка постов соответствию условий
        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModerationStatus().toString().equals("NEW")) {//только новые посты
                   arrayMapFormMetodsForModerationPosts.add(MetodsForPostController.createJsonForModerationPosts(post));
            }
            else {
                if (post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModeratorId() == userId) {//Посты соотвествующие статусу
                    arrayMapFormMetodsForModerationPosts.add(MetodsForPostController.createJsonForModerationPosts(post));
                }
            }
        });
        answerJson.put("count", arrayMapFormMetodsForModerationPosts.size());//Фиксируем количество постов
        // Определяемся с количеством выводимого на экран
        // Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForModerationPosts.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForModerationPosts.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForModerationPosts.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForModerationPosts.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер вывода постов пользователя
    @GetMapping ("/post/my")
    public String myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {
        ArrayList <Map> arrayMapFormMetodsForMyPosts = new ArrayList<>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Выбчбор статуса постов
        switch (status) {
            case ("inactive") :
                postsRepository.findAll().forEach(post -> {
                    if (!post.isActive() && post.getUser().getId() == userId) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
            case ("pending") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("NEW")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
            case ("declined") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("DECLINED")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
            case ("published") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
        }
        answerJson.put("count", arrayMapFormMetodsForMyPosts.size());//Фиксируем количество
        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (arrayMapFormMetodsForMyPosts.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForMyPosts.get(i));
            }
        }
        else {
            for (int i = offset; i < arrayMapFormMetodsForMyPosts.size(); i++) {
                arrayArrayForAnswer.add(arrayMapFormMetodsForMyPosts.get(i));
            }
        }
        answerJson.put("posts", arrayArrayForAnswer);
        return new Gson().toJson(answerJson);
    }

    //Контроллер создания поста
    @PostMapping ("/post")
    public String createPost (HttpServletRequest request,
                              @RequestBody Map<String, Object> information) throws ParseException  {
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        Map <String, String> errors = new HashMap<>();
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
        ArrayList <String> tags = (ArrayList<String>) information.get("tags");

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
            answerJson.put("result", false);
            answerJson.put("errors", errors);
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
            answerJson.put("result", true);
        }
        return new Gson().toJson(answerJson);
    }

    //Контроллер изменения поста по id
    @PutMapping ("/post/{id}")
    public String editPost (HttpServletRequest request,
                              @RequestParam("time") Date time,
                              @RequestParam("active") boolean active,
                              @RequestParam("title") String title,
                              @RequestParam("text") String text,
                              @RequestParam("tags") String tags,
                              @PathVariable int id) {

        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<String, String> errors = new HashMap<>();
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
            answerJson.put("result", false);
            answerJson.put("errors", errors);
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
            answerJson.put("result", true);
        }
        return new Gson().toJson(answerJson);
    }

    //Контроллер постановки лайка посту
    @PostMapping("/post/like")
    public String getLike (HttpServletRequest request,
                           @RequestParam("post_in") int postId) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();

        Integer userId = DefaultController.getIdUserLogin(request);
        //Проверка авторизации польлзователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Проверяем была ли уже оценка данного поста данным пользователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {//оценка была
                if (vote.isValue()) {
                    answerJson.put("result", false);//Если оценка была позитивна, то ничего не делаем
                }
                else {//Если оценка была негативна, то удаляем ее и ставим лайк
                    vote.setValue(true);
                    vote.setTime(new Date());
                    postsVotesRepository.save(vote);
                    answerJson.put("result", true);
                }
                return new Gson().toJson(answerJson);
            }
        }
        //Оценки поста данным пользователем не было, создаем новую оценку и связываем с постом
        PostsVotes vote = new PostsVotes();
        vote.setPostId(postId);
        vote.setUserId(userId);
        vote.setTime(new Date());
        vote.setValue(true);
        postsVotesRepository.save(vote);
        answerJson.put("result", true);
        return new Gson().toJson(answerJson);
    }

    //Контроллер постановки дизлайка
    @PostMapping("/post/dislike")
    public String getDislike (HttpServletRequest request,
                           @RequestParam("post_in") int postId) {
        Integer userId = DefaultController.getIdUserLogin(request);
        //Проверка авторизации польлзователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        Map<Object, Object> answerJson = new HashMap<Object, Object>();

        //Проверяем была ли уже оценка данного поста данным пользователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {
                if (!vote.isValue()) {
                    answerJson.put("result", false);//Если оценка была негативна, то ничего не делаем
                }
                else {//Если оценка была позитивна, то удаляем ее и ставим дизлайк
                    vote.setValue(false);
                    vote.setTime(new Date());
                    postsVotesRepository.save(vote);
                    answerJson.put("result", true);
                }
                return new Gson().toJson(answerJson);
            }
        }
        //Оценки поста данным пользователем не было, создаем новую оценку и связываем с постом
        PostsVotes vote = new PostsVotes();
        vote.setPostId(postId);
        vote.setUserId(userId);
        vote.setTime(new Date());
        vote.setValue(false);
        postsVotesRepository.save(vote);
        answerJson.put("result", true);
        return new Gson().toJson(answerJson);
    }

}
