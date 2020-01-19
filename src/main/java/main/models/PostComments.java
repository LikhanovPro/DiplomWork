package main.models;


import javax.persistence.*;
import java.util.Date;

@Entity
public class PostComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "parent_id")
    private int parentId;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column (nullable = false)
    private Date time;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "post_id")
    private Posts postForComments;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "user_id")
    private Users userForComments;

    //==================================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Posts getPostForComments() {
        return postForComments;
    }

    public void setPostForComments(Posts postForComments) {
        this.postForComments = postForComments;
    }

    public Users getUserForComments() {
        return userForComments;
    }

    public void setUserForComments(Users userForComments) {
        this.userForComments = userForComments;
    }
}
