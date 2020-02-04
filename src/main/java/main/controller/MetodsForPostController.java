package main.controller;

import main.models.Posts;
import main.models.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class MetodsForPostController {

    public static Map <Object, Object> createJsonForPostsList (Integer postId, PostsRepository postsRepository) {


        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

        mapForAnswer.put("id", postId);
        mapForAnswer.put("time", postsRepository.findById(postId).get().getTime().toString());
        userMap.put("id", postsRepository.findById(postId).get().getUser().getId());
        userMap.put("name", postsRepository.findById(postId).get().getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", postsRepository.findById(postId).get().getTitle());
        mapForAnswer.put("announce", "Не понял, что именно выводить");
        int likeCount = 0;
        int dislikeCounts = 0;
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

    public static Map <Object, Object> createJsonForPostById(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

        ArrayList <Map> commentsList = new ArrayList<>();



        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());

        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());
        mapForAnswer.put("text", post.getText());
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);


        post.getCommentsToPost().forEach(comment ->{
            Map <Object, Object> userComments = new HashMap<>();
            Map <Object, Object> commentsMap = new HashMap<>();
            commentsMap.put("id", comment.getId());
            commentsMap.put("time", comment.getTime());

            userComments.put("id", comment.getUserForComments().getId());
            userComments.put("name", comment.getUserForComments().getName());
            userComments.put("photo", comment.getUserForComments().getPhoto());
            commentsMap.put("user", userComments);
            commentsMap.put("text", comment.getComment());
            commentsList.add(commentsMap);
        });
        mapForAnswer.put("comments", commentsList);
        List <String> tags = new ArrayList<>();
        post.getTagsToPost().forEach(tag -> {
            tags.add(tag.getName());
        });
        mapForAnswer.put("tags", tags);
        return mapForAnswer;
    }

    public static Map <Object, Object> createJsonForModerationPosts(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());

        userMap.put("id", post.getUser().getId());
        userMap.put("name", post.getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", post.getTitle());
        mapForAnswer.put("announce", "Не понял, что именно выводить");

        return mapForAnswer;
    }

    public static Map <Object, Object> createJsonForMyPosts(Posts post) {
        Map <Object, Object> mapForAnswer = new HashMap<>();

        mapForAnswer.put("id", post.getId());
        mapForAnswer.put("time", post.getTime().toString());
        mapForAnswer.put("title", post.getTitle());
        mapForAnswer.put("announce", "Не понял, что именно выводить");
        int likeCount = 0;
        int dislikeCounts = 0;
        for (int i = 0; i < post.getVotesToPost().size(); i++) {
            if (post.getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", post.getCommentsToPost().size());
        mapForAnswer.put("viewCount", post.getViewCount());
        return mapForAnswer;
    }

}
