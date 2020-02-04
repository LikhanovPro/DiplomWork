package main.controller;

import main.models.Posts;
import main.models.PostsRepository;
import main.models.Users;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;



public class MetodsForAuthController {

    public static Map<Object, Object> createAuthInformation (Users user, PostsRepository postsRepository) {
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        Map <Object, Object> userMap = new HashMap<>();

        answerJson.put("result" , true);
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("photo", user.getPhoto());
        userMap.put("email", user.geteMail());
        userMap.put("moderation", user.isModerator());

        int moderationCount = 0;
        for (Posts post : postsRepository.findAll()) {
            if (post.getModeratorId() == user.getId()) {
                moderationCount++;
            }
        }
        userMap.put("moderationCount", moderationCount);
        userMap.put("settings", user.isModerator());
        answerJson.put("user", userMap);
        return answerJson;
    }
}
