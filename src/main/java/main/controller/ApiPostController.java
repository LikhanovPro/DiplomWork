package main.controller;

import main.models.*;
import main.requestObject.post.PostPostCreatePostObject;
import main.requestObject.post.PostPostDislikeObject;
import main.requestObject.post.PostPostLikeObject;
import main.response.post.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping ("/api")
public class ApiPostController extends HttpServlet {

    @Autowired
    PostGetPost postGetPost;

    private PostPostCreatePost addNewPost = new PostPostCreatePost();

    private PostGetSearch postGetSearch = new PostGetSearch();

   // private PostGetPost postGetPost = new PostGetPost();

    private PostGetById postGetById = new PostGetById();

    private PostGetByDate postGetByDate = new PostGetByDate();

    private PostGetByTag postGetByTag = new PostGetByTag();

    private PostGetPostsForModeration postGetPostsForModeration = new PostGetPostsForModeration();

    private PostGetMyPosts postGetMyPosts = new PostGetMyPosts();

    private PostPutPostById postPutPostById = new PostPutPostById();

    private PostPostLike postPostLike = new PostPostLike();

    private PostPostDislike postPostDislike = new PostPostDislike();

    /*//Подключаем репозитории
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
    //==========================================*/

    @GetMapping ("/post")
    public ResponseEntity postsList (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("mode") String mode) {
        return postGetPost.getPosts(offset, limit, mode);
    }

    //Контроллер поиска постов по словам
    @GetMapping ("/post/search")
    public ResponseEntity searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {
        return postGetSearch.getPosts(offset, limit, query);
    }

    //Контроллер вывод поста по id
    @GetMapping ("/post/{id}")
    public ResponseEntity postById (@PathVariable int id) {
        return postGetById.getPost(id);
    }

    //Контроллер получения постов по дате публикации
    @GetMapping ("/post/byDate")
    public ResponseEntity postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {
        return postGetByDate.getPost(offset, limit, date);
    }

    //Контроллер вывода постов по тегам
    @GetMapping ("/post/byTag")
    public ResponseEntity postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {
        return postGetByTag.getPost(offset, limit, tag);
    }

    //Контроллер вывода постов для модерации
    @GetMapping ("/post/moderation")
    public ResponseEntity moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit,
                                   @RequestParam ("status") String status) {
        return postGetPostsForModeration.moderationPost(request, offset, limit, status);
    }

    //Контроллер вывода постов пользователя
    @GetMapping ("/post/my")
    public ResponseEntity myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {
        return postGetMyPosts.getMyPost(request, offset, limit, status);
    }

    //Контроллер создания поста
    @PostMapping ("/post")
    public ResponseEntity createPost (HttpServletRequest request,
                              @RequestBody PostPostCreatePostObject information) throws ParseException  {
        return addNewPost.getPostPostCreatePost(request, information);
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
        return postPutPostById.changePost(request, time, active, title, text, tags, id);
    }

    //Контроллер постановки лайка посту
    @PostMapping("/post/like")
    public ResponseEntity getLike (HttpServletRequest request,
                                   @RequestBody PostPostLikeObject information) {
        return postPostLike.getLike(request, information);
    }

    //Контроллер постановки дизлайка
    @PostMapping("/post/dislike")
    public ResponseEntity getDislike (HttpServletRequest request,
                                      @RequestBody PostPostDislikeObject information) {
        return postPostDislike.getDislike(request, information);
    }
}
