package main.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table (name = "tags")
public class Tags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @ManyToMany (cascade = CascadeType.ALL)
    @JoinTable (name = "tag2post",
            joinColumns=@JoinColumn (name = "tag_id"),
            inverseJoinColumns = @JoinColumn (name = "post_id"))
    private List<Posts> postsForTags;

    //===============================================================================================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Posts> getPostsForTags() {
        return postsForTags;
    }

    public void setPostsForTags(List<Posts> postsForTags) {
        this.postsForTags = postsForTags;
    }
}
