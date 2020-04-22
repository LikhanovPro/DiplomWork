package main.service;

import main.models.*;
import main.requestObject.PostPostCreatePostObject;
import main.requestObject.PostPostDislikeObject;
import main.requestObject.PostPostLikeObject;
import main.requestObject.PostPutPostByIdObject;
import main.responseObject.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PostService {

    @Value("${diplomawork.titleLength}")
    int titleLength;
    @Value("${diplomawork.textLength}")
    int textLength;
    @Value("${diplomawork.pathToImages}")
    String pathToImages;
    @Value("${diplomawork.announceLength}")
    int announceLength;
    @Value("${diploma.dateformat.post}")
    String dateFormatPost;
    @Value("${diploma.dateformat.calendar}")
    String dateFormatCalendar;

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
    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    private SessionInformation sessionInformation;
    //==========================================

    public ResponseEntity <ResponseApi> postList (int offset, int limit, String mode) {
        PostGetPost postGetPost = new PostGetPost();
        ArrayList <Map> posts = new ArrayList<>();
        ArrayList<Map> allPosts = new ArrayList<>();
        //Выбор метода отображения списка постов
        switch (mode) {
            case ("recent"):
                Map<Integer, Date> idPostsListRecently = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListRecently.put(post.getId(), post.getTime());
                    }
                });
                postGetPost.setCount(idPostsListRecently.size());// Фиксируем количество постов
                idPostsListRecently.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue().reversed())//Сортировка по датам
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));
                break;
            case ("popular"):
                Map<Integer, Integer> idPostsListPopular = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListPopular.put(post.getId(), post.getCommentsToPost().size());
                    }
                });
                postGetPost.setCount(idPostsListPopular.size());//Фиксируем количество постов
                idPostsListPopular.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())//Сортировка по количеству коментариев
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));
                break;
            case ("best"):
                Map<Integer, Integer> idPostsListBest = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        //Считаем количество лайков для постов
                        int likeCount = 0;
                        for (int i = 0; i < post.getVotesToPost().size(); i++) {
                            if (post.getVotesToPost().get(i).isValue()) {
                                likeCount++;
                            }
                            idPostsListBest.put(post.getId(), likeCount);//Заполняем список
                        }
                    }
                });
                postGetPost.setCount(idPostsListBest.size());//Фиксируем количество постов
                idPostsListBest.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue())//Выполняем сортировку
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));
                break;
            case ("early"):
                Map<Integer, Date> idPostsListEarly = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListEarly.put(post.getId(), post.getTime());
                    }
                });
                postGetPost.setCount(idPostsListEarly.size());//Фиксируем количество постов
                idPostsListEarly.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue())//Сортируем по датам
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));
                break;
            default:
                postGetPost.setCount(0);
                break;
        }
        //Определяем количество выводимых на экран постов
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
        postGetPost.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetPost);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postSearch (int offset, int limit, String query) {
        PostGetSearch postGetSearch = new PostGetSearch();
        ArrayList <Map> posts = new ArrayList<>();
        ArrayList<Map> allPosts = new ArrayList<>();
        //Проверяем, что строка поиска не пуста
        if (!query.equals(null)) {
            postsRepository.findAll().forEach(post -> {
                //Сложный поиск по условиям:
                //1. Посты активны, и утверждены модератором
                //2. текст из строки поиска найден в тексте или заголовке поста
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&&
                        (post.getTitle().toUpperCase().contains(query.toUpperCase())||post.getText().toUpperCase().contains(query.toUpperCase()))) {
                    allPosts.add(getPostInformation(post.getId(), postsRepository));
                }
            });
        }
        else {//Если пуста, то выводим все посты
            postsRepository.findAll().forEach(post -> {
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                    allPosts.add(getPostInformation(post.getId(), postsRepository));
                }
            });
        }
        postGetSearch.setCount(allPosts.size());//Фиксируем количество постов
        //Определяем количество выводимых на экран постов
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
        postGetSearch.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetSearch);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postById (int id) {
        PostGetById postGetById = new PostGetById();
        Map <Object, Object> user = new HashMap<>();
        ArrayList<Map <Object, Object>> comments = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost);
        if (!postsRepository.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост с данным id не найден");
        }
        Posts post = postsRepository.findById(id).get();
        //Сложный запрос на соответствие поста условиям
        if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") && post.getTime().before(new Date())) {
            postGetById.setId(post.getId());
            postGetById.setTime(dateFormat.format(post.getTime()));
            user.put("id", post.getUser().getId());
            user.put("name", post.getUser().getName());
            postGetById.setUser(user);
            postGetById.setTitle(post.getTitle());
            postGetById.setText(post.getText());
            int likeCounts = 0;
            int dislikeCounts = 0;
            for (int i = 0; i < post.getVotesToPost().size(); i++) {
                if (post.getVotesToPost().get(i).isValue()){
                    likeCounts++;
                }
                else {
                    dislikeCounts++;
                }
            }
            postGetById.setLikeCount(likeCounts);
            postGetById.setDislikeCount(dislikeCounts);
            post.setViewCount(post.getViewCount() + 1);
            postsRepository.save(post);
            postGetById.setViewCount(post.getViewCount());
            post.getCommentsToPost().forEach(comment ->{
                Map <Object, Object> userComments = new HashMap<>();
                Map <Object, Object> commentsMap = new HashMap<>();
                commentsMap.put("id", comment.getId());
                commentsMap.put("time", dateFormat.format(comment.getTime()));
                userComments.put("id", comment.getUserForComments().getId());
                userComments.put("name", comment.getUserForComments().getName());
                userComments.put("photo", comment.getUserForComments().getPhoto());
                commentsMap.put("user", userComments);
                commentsMap.put("text", comment.getComment());
                comments.add(commentsMap);
            });
            postGetById.setComments(comments);
            post.getTagsToPost().forEach(tag -> {
                tags.add(tag.getName());
            });
            postGetById.setTags(tags);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост не активен или не опубликован");
        }
        return ResponseEntity.status(HttpStatus.OK).body(postGetById);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postByDate (int offset, int limit, String date) {
        PostGetByDate postGetByDate = new PostGetByDate();
        ArrayList <Map> posts = new ArrayList<>();
        ArrayList<Map> allPosts = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatCalendar); //Перенести в настройки, взять от туда
        //Проверяем все посты на соответствие условиям
        postsRepository.findAll().forEach(post -> {
            //Проверка постов указанной дате, которая была передана с frontend
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") &&
                    dateFormat.format(post.getTime()).equals(date)) {
                allPosts.add(getPostInformation(post.getId(), postsRepository));//Заполнение информации по посту
            }
        });
        postGetByDate.setCount(allPosts.size());//Фиксация количества постов
        //Определяем количество выводимых на экран постов
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
        postGetByDate.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetByDate);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postByTag (int offset, int limit, String tag) {
        PostGetByTag postGetByTag = new PostGetByTag();
        ArrayList <Map> posts = new ArrayList<>();
        ArrayList<Map> allPosts = new ArrayList<>();
        //Проверяем все посты
        postsRepository.findAll().forEach(post -> {
            //Проверка условий поста
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                post.getTagsToPost().forEach(tagToPost -> {
                    if (tagToPost.getName().equals(tag)) {//Проверка соответствия тегу
                        allPosts.add(getPostInformation(post.getId(), postsRepository));
                    }
                });
            }
        });
        postGetByTag.setCount(allPosts.size());//Фиксируем количество
        //Определяем количество выводимых на экран постов
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
        postGetByTag.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetByTag);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postModeration (HttpServletRequest request, int offset, int limit, String status) {
        PostGetPostsForModeration postGetPostsForModeration = new PostGetPostsForModeration();
        ArrayList <Map<Object, Object>> posts = new ArrayList<>();
        ArrayList<Map<Object, Object>> allPosts = new ArrayList<>();
        Integer userId = sessionInformation.getIdUserLogin(request);
        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Проверка постов соответствию условий
        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModerationStatus().toString().equals("NEW")) {//только новые посты
                allPosts.add(getPostInformationForModeration(post));
            }
            else {
                if (post.getModerationStatus().toString().equals(status.toUpperCase()) && post.getModeratorId() == userId) {//Посты соотвествующие статусу
                    allPosts.add(getPostInformationForModeration(post));
                }
            }
        });
        postGetPostsForModeration.setCount(allPosts.size());
        //Определяем количество выводимых на экран постов
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
        postGetPostsForModeration.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetPostsForModeration);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> myPosts (HttpServletRequest request, int offset, int limit, String status) {
        PostGetMyPosts postGetMyPosts = new PostGetMyPosts();
        ArrayList <Map<Object, Object>> posts = new ArrayList<>();
        ArrayList<Map<Object, Object>> allPosts = new ArrayList<>();
        Integer userId = sessionInformation.getIdUserLogin(request);
        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Выбор статуса постов
        switch (status) {
            case ("inactive") :
                postsRepository.findAll().forEach(post -> {
                    if (!post.isActive() && post.getUser().getId() == userId) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("pending") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("NEW")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("declined") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("DECLINED")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
            case ("published") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        allPosts.add(getMyPostInformation(post));
                    }
                });
                break;
        }
        postGetMyPosts.setCount(allPosts.size());
        //Определяем количество выводимых на экран постов
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
        postGetMyPosts.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postGetMyPosts);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> createPost (HttpServletRequest request, PostPostCreatePostObject information) throws ParseException {
        PostPostCreatePost postPostCreatePost = new PostPostCreatePost();
        Map <String, String> errors = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost);
        Integer userId = sessionInformation.getIdUserLogin(request);
        Date time = dateFormat.parse((information.getTime()).replaceAll("T", " "));//Дата передается со знаком "Т" между датой и временем,убираю вручную
        boolean active;
        if (information.getActive() == 1) {
            active = true;
        }
        else {
            active = false;
        }
        String title = information.getTitle();
        String text = information.getText();
        ArrayList<String> tags = information.getTags();
        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Проверка соответствия даты, не может быть будущее
        if (time.before(new Date())){
            time = new Date();
        }
        //Проверка соотвествия количества знаков заголовка и текста
        if (title.length() < titleLength) {
            errors.put("title", "Заголовок не установлен или слишком короткий");
            postPostCreatePost.setErrors(errors);
            postPostCreatePost.setResult(false);
        }
        else {
            if (text.length() < textLength) {
                errors.put("text", "Текст публикации слишком короткий");
                postPostCreatePost.setErrors(errors);
                postPostCreatePost.setResult(false);
            } else {
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
                Map<String, Integer> tagsNames = new HashMap<>();
                for (Tags tag : tagsRepository.findAll()) {
                    tagsNames.put(tag.getName(), tag.getId());
                }
                ArrayList<Integer> tagsId = new ArrayList<>();
                for (int i = 0; i < tags.size(); i++) {
                    if (tagsNames.containsKey(tags.get(i).replaceAll(",", ""))) {
                        tagsId.add(tagsNames.get(tags.get(i)));
                    } else {//Если тегов нет в БД, то создаем новый тег
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
                postPostCreatePost.setResult(true);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(postPostCreatePost);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> putPostById (HttpServletRequest request, PostPutPostByIdObject information) throws ParseException {
        PostPutPostById postPutPostById = new PostPutPostById();
        Map<String, String> errors = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost); //Перенести в астройки, взять от туда
        Integer userId = sessionInformation.getIdUserLogin(request);
        Date time = dateFormat.parse((information.getTime()).replaceAll("T", " "));//Дата передается со знаком "Т" ммежду датой и временем,убираю вручную
        boolean active = information.isActive();
        String title = information.getTitle();
        String text = information.getText();
        ArrayList<String> tags = information.getTags();
        userId = sessionInformation.getIdUserLogin(request);
        //Проверяем авторизацию пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Проверка соответствия условий заголовка и текста
        if (title.length() < titleLength) {
            errors.put("title", "Заголовок не установлен или слишком короткий");
            postPutPostById.setErrors(errors);
            postPutPostById.setResult(false);
        }
        else {
            if (text.length() < textLength) {
                errors.put("text", "Текст публикации слишком короткий");
                postPutPostById.setErrors(errors);
                postPutPostById.setResult(false);
            } else {
                //Првоерка соответствия времени
                if (time.before(new Date())) {
                    time = new Date();
                }
                //Получаем пост из БД по id переданному из frontend
                Posts post = postsRepository.findById(information.getId()).get();
                post.setActive(active);
                //Если пользователь модератор - то устанавливаем модератора посту, если нет, ставим посту статус NEW и ждем решения модератора
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
                for (int i = 0; i < tags.size(); i++) {
                    if (tagsNames.containsKey(tags.get(i).replaceAll(",", ""))) {
                        tagsId.add(tagsNames.get(tags.get(i)));
                    } else {
                        Tags newTag = new Tags();
                        newTag.setName(tags.get(i));
                        tagsId.add(tagsRepository.save(newTag).getId());
                    }
                }
                //Создание связи между тегами и постом
                tagsId.forEach(tagId -> {
                    Tag2Post tag2Post = new Tag2Post();
                    tag2Post.setTagId(tagId);
                    tag2Post.setPostId(postId);
                    tag2PostRepository.save(tag2Post);
                });
                postPutPostById.setResult(true);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(postPutPostById);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postLike (HttpServletRequest request, PostPostLikeObject information) {
        PostPostLike postPostLike = new PostPostLike();
        Integer userId = sessionInformation.getIdUserLogin(request);
        Integer postId = information.getPostId();
        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Проверка оценки поста текущим польлзователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {//оценка была
                if (vote.isValue()) {
                    postPostLike.setResult(false);//Если оценка была позитивна, то ничего не делаем
                }
                else {//Если оценка была негативна, то удаляем ее и ставим лайк
                    vote.setValue(true);
                    vote.setTime(new Date());
                    postsVotesRepository.save(vote);
                    postPostLike.setResult(true);
                }
                return ResponseEntity.status(HttpStatus.OK).body(postPostLike);
            }
        }
        //Оценки поста данным пользователем не было, создаем новую оценку и связываем с постом
        PostsVotes vote = new PostsVotes();
        vote.setPostId(postId);
        vote.setUserId(userId);
        vote.setTime(new Date());
        vote.setValue(true);
        postsVotesRepository.save(vote);
        postPostLike.setResult(true);
        return ResponseEntity.status(HttpStatus.OK).body(postPostLike);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> postDislike (HttpServletRequest request, PostPostDislikeObject information) {
        PostPostDislike postPostDislike = new PostPostDislike();
        Integer userId = sessionInformation.getIdUserLogin(request);
        Integer postId = information.getPostId();
        //Проверка авторизации пользователя
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Проверяем была ли уже оценка данного поста данным пользователем
        for (PostsVotes vote : postsVotesRepository.findAll()) {
            if (vote.getPostId() == postId && vote.getUserId() == userId) {
                if (!vote.isValue()) {
                    postPostDislike.setResult(false);//Если оценка была негативна, то ничего не делаем
                }
                else {//Если оценка была позитивна, то удаляем ее и ставим дизлайк
                    vote.setValue(false);
                    vote.setTime(new Date());
                    postsVotesRepository.save(vote);
                    postPostDislike.setResult(true);
                }
                return ResponseEntity.status(HttpStatus.OK).body(postPostDislike);
            }
        }
        //Оценки поста данным пользователем не было, создаем новую оценку и связываем с постом
        PostsVotes vote = new PostsVotes();
        vote.setPostId(postId);
        vote.setUserId(userId);
        vote.setTime(new Date());
        vote.setValue(false);
        postsVotesRepository.save(vote);
        postPostDislike.setResult(true);
        return ResponseEntity.status(HttpStatus.OK).body(postPostDislike);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity<byte[]> createImage (String imageFile) throws IOException {
        String pathToImageParts [] = imageFile.split("-");
        File file = new File(pathToImages + pathToImageParts[0] + File.separator + pathToImageParts[1] +
                File.separator + pathToImageParts[2] + File.separator + pathToImageParts[3]);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        byte[] med = Files.readAllBytes(file.toPath());
        return new ResponseEntity<>(med, headers, HttpStatus.OK);
    }
//---------------------------------------------------------------------------------------------------------------------
    //Формирование информации о посте для списка постов
    private Map <Object, Object> getMyPostInformation (Posts post) {
        Map<Object, Object> mapForAnswer = new HashMap<>();
        Map<Object, Object> userMap = new HashMap<>();
        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());
        mapForAnswer.put("title", post.getTitle());
        String announce;
        if (post.getText().length() < announceLength) {
            announce = post.getText();
        } else {
            announce = post.getText().substring(0, announceLength) + "...";
        }
        mapForAnswer.put("announce", announce);
        //В описательной части API запросов задания нет требований к этой информации в ответе, но
        //при анализе работы веб-страницы это необходимо для работы сайта
        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()) {
                likeCount++;
            } else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", post.getCommentsToPost().size());
        mapForAnswer.put("viewCount", post.getViewCount());
        return mapForAnswer;
    }
//---------------------------------------------------------------------------------------------------------------------

    private Map <Object, Object> getPostInformationForModeration (Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();
        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());
        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());
        String announce;
        if (post.getText().length() < announceLength) {
            announce = post.getText();
        }
        else {
            announce = post.getText().substring(0, announceLength) + "...";
        }
        mapForAnswer.put("announce", announce);
        return mapForAnswer;
    }
//---------------------------------------------------------------------------------------------------------------------

    //Метод формирования информации о постах
    public Map <Object, Object> getPostInformation (Integer postId, PostsRepository postsRepository) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost); //Перенести в настройки, взять от туда
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();
        //Заполнение информации
        mapForAnswer.put("id", postId);
        mapForAnswer.put("time", dateFormat.format(postsRepository.findById(postId).get().getTime()));
        userMap.put("id", postsRepository.findById(postId).get().getUser().getId());
        userMap.put("name", postsRepository.findById(postId).get().getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", postsRepository.findById(postId).get().getTitle());
        String announce;
        //Формирование анонса
        if (postsRepository.findById(postId).get().getText().length() < announceLength) {//Проверка, что длина текста больше максимальной длины анонса
            announce = postsRepository.findById(postId).get().getText();
        }
        else {
            announce = postsRepository.findById(postId).get().getText().substring(0, announceLength) + "...";//Добавление троеточия в конце анонса
        }
        mapForAnswer.put("announce", announce);
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
//---------------------------------------------------------------------------------------------------------------------
}
