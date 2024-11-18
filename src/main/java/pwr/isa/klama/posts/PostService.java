package pwr.isa.klama.posts;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.security.logging.ApiLogger;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<PostDTO> getAllPosts() {
        ApiLogger.logInfo("/api/v1/posts", "Getting all posts");
        return postRepository.findAll().stream()
                .map(post -> new PostDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getAuthor(),
                        post.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    public Optional<PostDTO> getPostById(Long postId)
    {
        ApiLogger.logInfo("/api/v1/posts/" + postId, "Getting post by id = " + postId);
        Optional<Post> post = postRepository.findById(postId);
        return post.map(value -> new PostDTO(
                value.getId(),
                value.getTitle(),
                value.getContent(),
                value.getAuthor(),
                value.getCreatedAt().toString()
        ));
    }

    public Map<String, Object> addPost(Post post) {
        ApiLogger.logInfo("/api/v1/authorized/admin/posts/add", "Adding post");

        User author = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setAuthorId(author); // Set the User object to the Post

        post.setCreatedAt(new Timestamp(new Date().getTime()));
        post.setUpdatedAt(new Timestamp(new Date().getTime()));
        Optional<Post> postOptional = postRepository.findPostByTitle(post.getTitle());
        if (postOptional.isPresent()) {
            throw new IllegalStateException("Post z tytułem " + post.getTitle() + " już istnieje");
        }
        postRepository.save(post);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post został dodany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deletePost(Long postId) {
        ApiLogger.logInfo("/api/v1/authorized/admin/posts/delete/" + postId, "Deleting post by id = " + postId);
        boolean exists = postRepository.existsById(postId);
        if (!exists) {
            throw new IllegalStateException("Post o id " + postId + " nie istnieje, nie można go usunąć");
        }
        postRepository.deleteById(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> updatePost(Long postId, Post post) {
        ApiLogger.logInfo("/api/v1/authorized/admin/posts/update/" + postId, "Updating post by id = " + postId);
        if (post.getTitle() == null && post.getContent() == null) {
            throw new IllegalStateException("Tytuł i treść nie mogą być puste");
        }
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post o id " + postId + "nie istnieje");
        }

        Post postToUpdate = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post o id " + postId + " nie istnieje"));

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
        postRepository.save(postToUpdate);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<Post> getAllPostsByUser(Long userId) {
        ApiLogger.logInfo("/api/v1/posts/user/" + userId, "Getting all posts by user id = " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik o id " + userId + " nie istnieje"));
        return postRepository.findAllByAuthorId(user);
    }

    public List<Post> getAllPostsAdmin() {
        ApiLogger.logInfo("/api/v1/authorized/admin/posts", "Getting all posts as admin");
        return postRepository.findAll();
    }
}
