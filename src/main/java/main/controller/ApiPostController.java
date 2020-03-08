package main.controller;

import main.models.*;
import main.service.post.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
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
    public ResponseEntity postsList (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("mode") String mode) {

        PostGetPost posts = new PostGetPost(offset, limit, mode, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    //Контроллер поиска постов по словам
    @GetMapping ("/post/search")
    public ResponseEntity searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {

        PostGetSearch searchPost = new PostGetSearch(offset, limit, query, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(searchPost);
    }

    //Контроллер вывод поста по id
    @GetMapping ("/post/{id}")
    public ResponseEntity postById (@PathVariable int id) {

        PostGetById postById = new PostGetById(id, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(postById);
    }

    //Контроллер получения постов по дате публикации
    @GetMapping ("/post/byDate")
    public ResponseEntity postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {

        PostGetByDate postByDate = new PostGetByDate(offset, limit, date, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(postByDate);
    }

    //Контроллер вывода постов по тегам
    @GetMapping ("/post/byTag")
    public ResponseEntity postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {

        PostGetByTag postByTag = new PostGetByTag(offset, limit, tag, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(postByTag);
    }

    //Контроллер вывода постов для модерации
    @GetMapping ("/post/moderation")
    public ResponseEntity moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit,
                                   @RequestParam ("status") String status) {

        PostGetPostsForModeration postsForModeration = new PostGetPostsForModeration(request, offset, limit, status, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(postsForModeration);
    }

    //Контроллер вывода постов пользователя
    @GetMapping ("/post/my")
    public ResponseEntity myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {

        PostGetMyPosts myPosts = new PostGetMyPosts(request, offset, limit, status, postsRepository);

        return ResponseEntity.status(HttpStatus.OK).body(myPosts);
    }

    //Контроллер создания поста
    @PostMapping ("/post")
    public ResponseEntity createPost (HttpServletRequest request,
                              @RequestBody Map<String, Object> information) throws ParseException  {

        PostPostCreatePost createPost = new PostPostCreatePost(request, information, postsRepository, tagsRepository, tag2PostRepository);

        return ResponseEntity.status(HttpStatus.OK).body(createPost);
    }

    //Контроллер изменения поста по id
    @PutMapping ("/post/{id}")
    public ResponseEntity editPost (HttpServletRequest request,
                              @RequestParam("time") Date time,
                              @RequestParam("active") boolean active,
                              @RequestParam("title") String title,
                              @RequestParam("text") String text,
                              @RequestParam("tags") String tags,
                              @PathVariable int id) {

        PostPutPostById changePostById = new PostPutPostById(request, time, active, title, text, tags, id, usersRepository,
                postsRepository, tagsRepository, tag2PostRepository);

        return ResponseEntity.status(HttpStatus.OK).body(changePostById);
    }

    //Контроллер постановки лайка посту
    @PostMapping("/post/like")
    public ResponseEntity getLike (HttpServletRequest request,
                                   @RequestBody Map<String, Object> information) {

        PostPostLike getLike = new PostPostLike(request, information, postsVotesRepository);

        return ResponseEntity.status(HttpStatus.OK).body(getLike);
    }

    //Контроллер постановки дизлайка
    @PostMapping("/post/dislike")
    public ResponseEntity getDislike (HttpServletRequest request,
                                      @RequestBody Map<String, Object> information) {

        PostPostDislike getDislike = new PostPostDislike(request, information, postsVotesRepository);

        return ResponseEntity.status(HttpStatus.OK).body(getDislike);
    }
}
