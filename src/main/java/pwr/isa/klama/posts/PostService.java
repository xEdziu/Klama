package pwr.isa.klama.posts;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<PostDTO> getAllPosts() {
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
        post.setCreatedAt(new Timestamp(new Date().getTime()));
        post.setUpdatedAt(new Timestamp(new Date().getTime()));
        Optional<Post> postOptional = postRepository.findPostByTitle(post.getTitle());
        if (postOptional.isPresent()) {
            throw new IllegalStateException("Post o podanym tytule już istnieje");
        }
        System.out.println(post);
        postRepository.save(post);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post został dodany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deletePost(Long postId) {
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

    @Transactional
    public Map<String, Object> updatePost(Long postId, Post post) {
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
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }
}
