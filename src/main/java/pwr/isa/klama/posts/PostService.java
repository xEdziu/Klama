package pwr.isa.klama.posts;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long postId)
    {
        if (!postRepository.existsById(postId)) {
            throw new IllegalStateException("Post o id " + postId + " nie istnieje");
        }
        return postRepository.findById(postId);
    }

    public void addPost(Post post) {
        post.setCreatedAt(new Timestamp(new Date().getTime()));
        post.setUpdatedAt(new Timestamp(new Date().getTime()));
        Optional<Post> postOptional = postRepository.findPostByTitle(post.getTitle());
        if (postOptional.isPresent()) {
            throw new IllegalStateException("Post o podanym tytule już istnieje");
        }
        System.out.println(post);
        postRepository.save(post);
    }

    public void deletePost(Long postId) {
        boolean exists = postRepository.existsById(postId);
        if (!exists) {
            throw new IllegalStateException("Post o id " + postId + " nie istnieje");
        }
        postRepository.deleteById(postId);
    }

    @Transactional
    public void updatePost(Long postId, Post post) {
        if (post.getTitle() == null && post.getContent() == null) {
            throw new IllegalStateException("Tytuł i treść nie mogą być puste");
        }
        if (!postRepository.existsById(postId)) {
            throw new IllegalStateException("Post o id \" + postId + \" nie istnieje");
        }

        Post postToUpdate = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("PPost o id \" + postId + \" nie istnieje"));

        if (post.getTitle() != null) {
            if (post.getTitle().isEmpty()) {
                throw new IllegalStateException("Tytuł nie może być pusty");
            }
            postToUpdate.setTitle(post.getTitle());
        }

        if (post.getContent() != null) {
            if (post.getContent().isEmpty()) {
                throw new IllegalStateException("Zawartość nie może być pusta");
            }
            postToUpdate.setContent(post.getContent());
        }

        postToUpdate.setUpdatedAt(new Timestamp(new Date().getTime()));
    }
}
