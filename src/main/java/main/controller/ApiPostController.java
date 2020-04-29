package main.controller;

import main.requestObject.PostPostCreatePostObject;
import main.requestObject.PostPostDislikeObject;
import main.requestObject.PostPostLikeObject;
import main.requestObject.PostPutPostByIdObject;
import main.service.PostService;
import main.responseObject.ResponseApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;

@RestController
public class ApiPostController extends HttpServlet {

    @Autowired
    PostService postService;

    @GetMapping ("/api/post")
    public ResponseEntity<ResponseApi> postsList (@RequestParam("offset") int offset,
                                                   @RequestParam ("limit") int limit,
                                                   @RequestParam ("mode") String mode) {
        return postService.postList(offset, limit, mode);
    }

    //Контроллер поиска постов по словам
    @GetMapping ("/api/post/search")
    public ResponseEntity<ResponseApi> searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {
        return postService.postSearch(offset, limit, query);
    }

    //Контроллер вывод поста по id
    @GetMapping ("/api/post/{id}")
    public ResponseEntity <ResponseApi> postById (@PathVariable int id) {
        return postService.postById(id);
    }

    //Контроллер получения постов по дате публикации
    @GetMapping ("/api/post/byDate")
    public ResponseEntity<ResponseApi> postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {
        return postService.postByDate(offset, limit, date);
    }

    //Контроллер вывода постов по тегам
    @GetMapping ("/api/post/byTag")
    public ResponseEntity<ResponseApi> postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {
        return postService.postByTag(offset, limit, tag);
    }

    //Контроллер вывода постов для модерации
    @GetMapping ("/api/post/moderation")
    public ResponseEntity<ResponseApi> moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit,
                                   @RequestParam ("status") String status) {
        return postService.postModeration(request, offset, limit, status);
    }

    //Контроллер вывода постов пользователя
    @GetMapping ("/api/post/my")
    public ResponseEntity<ResponseApi> myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {
        return postService.myPosts(request, offset, limit, status);
    }

    //Контроллер создания поста
    @PostMapping ("/api/post")
    public ResponseEntity<ResponseApi> createPost (HttpServletRequest request,
                              @RequestBody PostPostCreatePostObject information) throws ParseException  {
        return postService.createPost(request, information);
    }

    //Контроллер изменения поста по id
    @PutMapping ("/api/post/{id}")
    public ResponseEntity<ResponseApi> editPost (HttpServletRequest request,
                                    @RequestBody PostPutPostByIdObject information) throws ParseException {
        return postService.putPostById(request, information);
    }

    //Контроллер постановки лайка посту
    @PostMapping("/api/post/like")
    public ResponseEntity<ResponseApi> getLike (HttpServletRequest request,
                                   @RequestBody PostPostLikeObject information) {
        return postService.postLike(request, information);
    }

    //Контроллер постановки дизлайка
    @PostMapping("/api/post/dislike")
    public ResponseEntity<ResponseApi> getDislike (HttpServletRequest request,
                                      @RequestBody PostPostDislikeObject information) {
        return postService.postDislike(request, information);
    }

    //Контроллер возврата изображения
    @RequestMapping(value = "image/{pathToFolder}/{imageFile}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImageAsResource(@PathVariable String pathToFolder, @PathVariable String imageFile) throws IOException {
        return postService.createImage(pathToFolder, imageFile);
    }

    //Контроллер возврата изображения
    @RequestMapping(value = "/post/image/{pathToFolder}/{imageFile}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImageAsResourceForComments(@PathVariable String pathToFolder, @PathVariable String imageFile) throws IOException {
        return postService.createImage(pathToFolder, imageFile);
    }
}
