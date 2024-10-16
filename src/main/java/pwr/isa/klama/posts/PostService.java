package pwr.isa.klama.posts;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class PostService {
    public List<Post> getPosts() {
        return List.of(
                new Post(1L, "First post", "Content of the first post", 1L, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime())),
                new Post(2L, "Second post", "Content of the second post", 1L, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()))
        );
    }
}
