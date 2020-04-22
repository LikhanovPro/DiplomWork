package main.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table (name = "post_votes")
public class PostsVotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name ="user_id", nullable = false)
    private int userId;

    @Column (name ="post_id", nullable = false)
    private int postId;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private boolean value;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "post_id", insertable = false, updatable = false)
    private Posts postForVote;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "user_id", insertable = false, updatable = false)
    private Users userForVote;

    //=================================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Posts getPostForVote() {
        return postForVote;
    }

    public void setPostForVote(Posts postForVote) {
        this.postForVote = postForVote;
    }

    public Users getUserForVote() {
        return userForVote;
    }

    public void setUserForVote(Users userForVote) {
        this.userForVote = userForVote;
    }
}
