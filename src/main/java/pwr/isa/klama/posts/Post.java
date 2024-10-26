package pwr.isa.klama.posts;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pwr.isa.klama.user.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table
@Getter
@Setter
public class Post {
    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    private Long id;
    private String title;
    private String content;
    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "author_id"
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User authorId;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public Post() {
    }

    public Post(Long id,
                String title,
                String content,
                User authorId,
                Timestamp createdAt,
                Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Post(String title, String content, User authorId, Timestamp createdAt, Timestamp updatedAt) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public String getAuthor() {
        return authorId.getUsername();
    }
}
