package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping ("/api")
public class ApiPostController {

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

        System.out.println(offset + limit + mode);
        Collection collection = new ArrayList();
        Map <String, ArrayList> keyArray = new HashMap<>();
        ArrayList <ArrayList> arrayListArray = new ArrayList<>();
        Map <String, Integer> strInteger = new HashMap<>();

        strInteger.put("count", (int) postsRepository.count());
        collection.add(strInteger);
        //strInteger.clear();
        for (Posts post : postsRepository.findAll()) {
            Map <String, String> strStr = new HashMap<>();
            Map <String, Integer> strIntegerUser = new HashMap<>();
            Map <String, String> strStrUser = new HashMap<>();
            Map <String, Map> keyMap = new HashMap<>();
            ArrayList <Map> arrayListMap = new ArrayList<>();

            strInteger.put("id", post.getId());
            int likes = 0;
            int dislikes = 0;
            for (PostsVotes postsVotes : postsVotesRepository.findAll()) {
                if (postsVotes.getPostId() == post.getId() && postsVotes.isValue()) {
                    likes++;
                }
                if (postsVotes.getPostId() == post.getId() && !postsVotes.isValue()) {
                    dislikes++;
                }
            }
            strInteger.put("likeCount", likes);
            strInteger.put("dislikeCount", dislikes);
            strInteger.put("commentCount", 111);
            strInteger.put("viewCount", post.getViewCount());
            strStr.put("time", post.getTime().toString());
            strStr.put("title", post.getTitle());
            strStr.put("announce", "111");
            strIntegerUser.put("id", post.getUserId());
            strStrUser.put("name", usersRepository.findById(post.getUserId()).get().getName());
            keyArray.put("user", arrayListMap);
            arrayListMap.clear();
            arrayListMap.add(strInteger);
            arrayListMap.add(strStr);
            arrayListMap.add(keyArray);

            arrayListArray.add(arrayListMap);
            arrayListMap.clear();
        }
        keyArray.put("posts", arrayListArray);
        collection.add(keyArray);

        return new Gson().toJson(collection);
    }







}
