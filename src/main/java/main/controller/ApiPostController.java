package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping ("/api")
public class ApiPostController extends HttpServlet {

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

    @GetMapping ("/post")
    public String postsList (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("mode") String mode) {

        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();

        switch (mode) {
            case ("recent") :
                Map <Integer, Date> idPostsListRecently = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListRecently.put(post.getId(), post.getTime());
                    }
                });
                answerJson.put("count", idPostsListRecently.size());
                idPostsListRecently.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue().reversed())
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));
                break;
            case ("popular") :
                Map <Integer, Integer> idPostsListPopular = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListPopular.put(post.getId(), post.getCommentsToPost().size());
                    }
                });
                answerJson.put("count", idPostsListPopular.size());
                idPostsListPopular.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));
                break;
            case ("best") :
                Map <Integer, Integer> idPostsListBest = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {

                        int likeCount = 0;
                        for (int i = 0; i < post.getVotesToPost().size(); i++) {
                            if (post.getVotesToPost().get(i).isValue()){
                                likeCount++;
                            }
                        idPostsListBest.put(post.getId(), likeCount);
                    }
                }
                });
                answerJson.put("count", idPostsListBest.size());
                idPostsListBest.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue())
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));

                break;
            case ("early") :
                Map <Integer, Date> idPostsListEarly = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                        idPostsListEarly.put(post.getId(), post.getTime());
                    }
                });
                answerJson.put("count", idPostsListEarly.size());
                idPostsListEarly.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue())
                        .forEach(postId -> arrayMapFormMetodsForPostController
                                .add(MetodsForPostController.createJsonForPostsList(postId.getKey(), postsRepository)));
                break;
            default:
                answerJson.put("count", 0);
                break;
        }


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

    @GetMapping ("/post/search")
    public String searchPosts (@RequestParam("offset") int offset,
                             @RequestParam ("limit") int limit,
                             @RequestParam ("query") String query) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();


        if (!query.equals(null)) {
            postsRepository.findAll().forEach(post -> {
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&&
                        (post.getTitle().toUpperCase().contains(query.toUpperCase())||post.getText().toUpperCase().contains(query.toUpperCase()))) {
                    arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                }
            });
        }
        else {
            postsRepository.findAll().forEach(post -> {
                if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                    arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                }
            });

        }
        answerJson.put("count", arrayMapFormMetodsForPostController.size());
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

    @GetMapping ("/post/{id}")
    public String postById (@PathVariable int id) {
        Map <Object, Object> answerJson = new HashMap<Object, Object>();

        Posts post = postsRepository.findById(id).get();
        if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") && post.getTime().before(new Date())) {
            answerJson = MetodsForPostController.createJsonForPostById(post);
        }
        return new Gson().toJson(answerJson);
    }

    @GetMapping ("/post/byDate")
    public String postsByDate (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("date") String date) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") &&
            dateFormat.format(post.getTime()).equals(date)) {
                arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
            }
        });

        answerJson.put("count", arrayMapFormMetodsForPostController.size());
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

    @GetMapping ("/post/byTag")
    public String postsByTag (@RequestParam("offset") int offset,
                               @RequestParam ("limit") int limit,
                               @RequestParam ("tag") String tag) {
        ArrayList <Map> arrayMapFormMetodsForPostController = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();

        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                post.getTagsToPost().forEach(tagToPost -> {
                    if (tagToPost.getName().equals(tag)) {
                        arrayMapFormMetodsForPostController.add(MetodsForPostController.createJsonForPostsList(post.getId(), postsRepository));
                    }
                });
            }
        });
        answerJson.put("count", arrayMapFormMetodsForPostController.size());
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

    @GetMapping ("/post/moderation")
    public String moderationPosts (HttpServletRequest request,
                                   @RequestParam("offset") int offset,
                                   @RequestParam ("limit") int limit) {
        ArrayList <Map> arrayMapFormMetodsForModerationPosts = new ArrayList<>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();

        int userId = ApiAuthController.getIdUserLogin(request);
        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                if (post.getModerationStatus().equals("NEW") || post.getModeratorId() == userId) {
                    arrayMapFormMetodsForModerationPosts.add(MetodsForPostController.createJsonForModerationPosts(post));
                }
            }
        });
        answerJson.put("count", arrayMapFormMetodsForModerationPosts.size());
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

    @GetMapping ("/post/my")
    public String myPosts (HttpServletRequest request,
                           @RequestParam("offset") int offset,
                           @RequestParam ("limit") int limit,
                           @RequestParam ("status") String status) {
        ArrayList <Map> arrayMapFormMetodsForMyPosts = new ArrayList<>();
        ArrayList <Map> arrayArrayForAnswer = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        int userId = ApiAuthController.getIdUserLogin(request);

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
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().equals("NEW")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
            case ("declined") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().equals("DECLINED")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
            case ("published") :
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getUser().getId() == userId && post.getModerationStatus().equals("ACCEPTED")) {
                        arrayMapFormMetodsForMyPosts.add(MetodsForPostController.createJsonForMyPosts(post));
                    }
                });
                break;
        }
        answerJson.put("count", arrayMapFormMetodsForMyPosts.size());
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

    @PostMapping ("/post")
    public String createPost (HttpServletRequest request,
                              @RequestParam("time") Date time,
                              @RequestParam("active") boolean active,
                              @RequestParam("title") String title,
                              @RequestParam("text") String text,
                              @RequestParam("tags") String tags) {
        int userId = ApiAuthController.getIdUserLogin(request);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
        String [] tagsList;
        tags.replaceAll(", ", ",");
        tagsList = tags.split(",");
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        Map <String, String> errors = new HashMap<>();


        if (time.before(new Date())){
            time = new Date();
        }

        if (title.length() < 10 || text.length() < 500) {
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
            post.setViewCount(null);
            int postId = postsRepository.save(post).getId();

            //Поиск тегов, сохранение и получение их Id - при отладке вынести во внешний метод
            Map <String, Integer> tagsNames = new HashMap<>();
            for (Tags tag : tagsRepository.findAll()) {
                tagsNames.put(tag.getName(), tag.getId());
            }
            ArrayList <Integer> tagsId = new ArrayList<>();
            for (int i = 0; i < tagsList.length; i++) {
                if (tagsNames.containsKey(tagsList[i].replaceAll(",", ""))) {
                    tagsId.add(tagsNames.get(tagsList[i]));
                }
                else {
                    Tags newTag = new Tags();
                    newTag.setName(tagsList[i]);
                    tagsId.add(tagsRepository.save(newTag).getId());
                }
            }
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

        if (title.length() < 10 || text.length() < 500) {
            errors.put("title", "Заголовок не установлен");
            errors.put("text", "Текст публикации слишком короткий");
            answerJson.put("result", false);
            answerJson.put("errors", errors);
        } else {
            int userId = ApiAuthController.getIdUserLogin(request);
            String[] tagsList;
            tags.replaceAll(", ", ",");
            tagsList = tags.split(",");
            if (time.before(new Date())) {
                time = new Date();
            }

            Posts post = postsRepository.findById(id).get();
            post.setActive(active);
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




}
