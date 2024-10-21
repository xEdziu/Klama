package pwr.isa.klama.posts;

import lombok.Getter;

@Getter
public class PostDTO {
    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final String createdAt;

    public PostDTO(Long id, String title, String content, String author, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

}
