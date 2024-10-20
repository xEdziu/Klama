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
            throw new IllegalStateException("Post with id " + postId + " does not exist");
        }
        return postRepository.findById(postId);
    }

    public void addPost(Post post) {
        post.setCreatedAt(new Timestamp(new Date().getTime()));
        post.setUpdatedAt(new Timestamp(new Date().getTime()));
        Optional<Post> postOptional = postRepository.findPostByTitle(post.getTitle());
        if (postOptional.isPresent()) {
            throw new IllegalStateException("Post with given title already exists");
        }
        System.out.println(post);
        postRepository.save(post);
    }

    public void deletePost(Long postId) {
        boolean exists = postRepository.existsById(postId);
        if (!exists) {
            throw new IllegalStateException("Post with id " + postId + " does not exist");
        }
        postRepository.deleteById(postId);
    }

    @Transactional
    public void updatePost(Long postId, Post post) {
        if (post.getTitle() == null && post.getContent() == null) {
            throw new IllegalStateException("Title or content must be provided to update post");
        }
        if (!postRepository.existsById(postId)) {
            throw new IllegalStateException("Post with id " + postId + " does not exist");
        }

        Post postToUpdate = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("Post with id " + postId + " does not exist"));

        if (post.getTitle() != null) {
            if (post.getTitle().isEmpty() || Objects.equals(post.getTitle(), postToUpdate.getTitle())) {
                throw new IllegalStateException("Title must not be empty or the same as the current one");
            }
            postToUpdate.setTitle(post.getTitle());
        }

        if (post.getContent() != null) {
            if (post.getContent().isEmpty() || Objects.equals(post.getContent(), postToUpdate.getContent())) {
                throw new IllegalStateException("Content must not be empty or the same as the current one");
            }
            postToUpdate.setContent(post.getContent());
        }

        postToUpdate.setUpdatedAt(new Timestamp(new Date().getTime()));
    }
}
