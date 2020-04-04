package main.controller;

import main.models.*;
import main.requestObject.PostPostCreatePostObject;
import main.requestObject.PostPostDislikeObject;
import main.requestObject.PostPostLikeObject;
import main.requestObject.PostPutPostByIdObject;
import main.responseObject.*;
import main.service.PostService;
import main.service.ResponseApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.ServletContextResource;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;



@RestController
@RequestMapping ("/api")
public class ApiPostController extends HttpServlet {

    @Autowired
    PostService postService;

    @GetMapping ("/post")
    public ResponseEntity<ResponseApi> postsList (@RequestParam("offset") int offset,
                                                   @RequestParam ("limit") int limit,
                                                   @RequestParam ("mode") String mode) {
        return postService.postList(offset, limit, mode);
    }

    //Контроллер поиска постов по словам
    @GetMapping ("/post/search")
    public ResponseEntity<ResponseApi> searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {
        return postService.postSearch(offset, limit, query);
    }

    //Контроллер вывод поста по id
    @GetMapping ("/post/{id}")
    public ResponseEntity <ResponseApi> postById (@PathVariable int id) {
        return postService.postById(id);
    }

    //Контроллер получения постов по дате публикации
    @GetMapping ("/post/byDate")
    public ResponseEntity<ResponseApi> postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {
        return postService.postByDate(offset, limit, date);
    }

    //Контроллер вывода постов по тегам
    @GetMapping ("/post/byTag")
    public ResponseEntity<ResponseApi> postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {
        return postService.postByTag(offset, limit, tag);
    }

    //Контроллер вывода постов для модерации
    @GetMapping ("/post/moderation")
    public ResponseEntity<ResponseApi> moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit,
                                   @RequestParam ("status") String status) {
        return postService.postModeration(request, offset, limit, status);
    }

    //Контроллер вывода постов пользователя
    @GetMapping ("/post/my")
    public ResponseEntity<ResponseApi> myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {
        return postService.myPosts(request, offset, limit, status);
    }

    //Контроллер создания поста
    @PostMapping ("/post")
    public ResponseEntity<ResponseApi> createPost (HttpServletRequest request,
                              @RequestBody PostPostCreatePostObject information) throws ParseException  {
        return postService.createPost(request, information);
    }

    //Контроллер изменения поста по id
    @PutMapping ("/post/{id}")
    public ResponseEntity<ResponseApi> editPost (HttpServletRequest request,
                                    @RequestBody PostPutPostByIdObject information) throws ParseException {
        return postService.putPostById(request, information);
    }

    //Контроллер постановки лайка посту
    @PostMapping("/post/like")
    public ResponseEntity<ResponseApi> getLike (HttpServletRequest request,
                                   @RequestBody PostPostLikeObject information) {
        return postService.postLike(request, information);
    }

    //Контроллер постановки дизлайка
    @PostMapping("/post/dislike")
    public ResponseEntity<ResponseApi> getDislike (HttpServletRequest request,
                                      @RequestBody PostPostDislikeObject information) {
        return postService.postDislike(request, information);
    }

    //Контроллер возврата изображения
    @RequestMapping(value = "/post/src/main/resources/static/upload/{level1}/{level2}/{level3}/{imageFile}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImageAsResource(@PathVariable String level1,
                                                       @PathVariable String level2,
                                                       @PathVariable String level3,
                                                       @PathVariable String imageFile) throws IOException {
        return postService.createImage(level1, level2, level3, imageFile);
    }
}
