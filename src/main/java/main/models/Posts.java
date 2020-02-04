package main.models;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table (name = "posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModeratorStatus moderationStatus;

    @Column(name = "moderator_id")
    private Integer moderatorId;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "user_id", insertable = false, updatable = false)
    private Users user;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String text;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @OneToMany (cascade = CascadeType.ALL, mappedBy = "postId")
    private List<PostComments> commentsToPost;

    @ManyToMany (cascade = CascadeType.ALL)
    @JoinTable (name = "tag2post",
                joinColumns=@JoinColumn (name = "post_id"),
                inverseJoinColumns = @JoinColumn (name = "tag_id"))
    private List <Tags> tagsToPost;

    @OneToMany (cascade = CascadeType.ALL, mappedBy = "postId")
    private List <PostsVotes> votesToPost;
    //===================================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ModeratorStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModeratorStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Integer getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(Integer moderatorId) {
        this.moderatorId = moderatorId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public List<PostComments> getCommentsToPost() {
        return commentsToPost;
    }

    public void setCommentsToPost(List<PostComments> commentsToPost) {
        this.commentsToPost = commentsToPost;
    }

    public List<Tags> getTagsToPost() {
        return tagsToPost;
    }

    public void setTagsToPost(List<Tags> tagsToPost) {
        this.tagsToPost = tagsToPost;
    }

    public List<PostsVotes> getVotesToPost() {
        return votesToPost;
    }

    public void setVotesToPost(List<PostsVotes> votesToPost) {
        this.votesToPost = votesToPost;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
