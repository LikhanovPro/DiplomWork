package main.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.models.Posts;
import main.models.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class PostGetById {

    @Autowired
    private PostsRepository postsRepository;

    @JsonProperty
    int id;

    @JsonProperty
    String time;

    @JsonProperty
    Map <Object, Object> user = new HashMap<>();

    @JsonProperty
    String title;

    @JsonProperty
    String text;

    @JsonProperty
    int likeCount;

    @JsonProperty
    int dislikeCount;

    @JsonProperty
    ArrayList<Map> comments = new ArrayList<>();

    @JsonProperty
    List<String> tags = new ArrayList<>();

    private PostGetById (int id) {

        //Получаем пост по id номеру
        Posts post = postsRepository.findById(id).get();

        //Сложный запрос на соответствие поста условиям
        if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED") && post.getTime().before(new Date())) {
            this.id = post.getId();
            time = post.getTime().toString();
            user.put("id", post.getUser().getId());
            user.put("name", post.getUser().getName());
            //mapForAnswer.put("user", userMap);
            title = post.getTitle();
            text = post.getText();
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
            likeCount = likeCounts;
            dislikeCount = dislikeCounts;

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
                comments.add(commentsMap);
            });
            post.getTagsToPost().forEach(tag -> {
                this.tags.add(tag.getName());
            });
        }
    }

    public PostGetById () {}

    public ResponseEntity getPost (int id) {
        return ResponseEntity.status(HttpStatus.OK).body(new PostGetById(id));
    }
}
