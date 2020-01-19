package main.models;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table (name = "users")
public class Users {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_moderator", nullable = false)
    private boolean isModerator;

    @Column(name = "reg_time", nullable = false)
    private Date regTime;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String eMail;

    @Column(nullable = false)
    private String password;

    private String code;

    private String photo;

    @OneToMany (cascade = CascadeType.ALL, mappedBy = "users")
    private List<Posts> userPosts;

    @OneToMany (cascade = CascadeType.ALL, mappedBy = "users")
    private List<PostComments> userComments;

    @OneToMany (cascade = CascadeType.ALL, mappedBy = "users")
    private List <PostsVotes> userVotes;

    //====================================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public void setModerator(boolean moderator) {
        isModerator = moderator;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Posts> getUserPosts() {
        return userPosts;
    }

    public void setUserPosts(List<Posts> userPosts) {
        this.userPosts = userPosts;
    }

    public List<PostComments> getUserComments() {
        return userComments;
    }

    public void setUserComments(List<PostComments> userComments) {
        this.userComments = userComments;
    }

    public List<PostsVotes> getUserVotes() {
        return userVotes;
    }

    public void setUserVotes(List<PostsVotes> userVotes) {
        this.userVotes = userVotes;
    }
}
