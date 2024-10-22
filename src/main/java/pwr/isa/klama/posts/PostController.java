package pwr.isa.klama.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pwr.isa.klama.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/")
public class PostController {

        private final PostService postService;

        @Autowired
        public PostController(PostService postService) {
                this.postService = postService;
        }

        @GetMapping("/posts")
        public List<PostDTO> getPosts() {
            return postService.getAllPosts();
        }

        @GetMapping(path = "/posts/{postId}")
        public PostDTO getPost(@PathVariable("postId") Long postId) {
            Optional<PostDTO> post = postService.getPostById(postId);
            if (post.isEmpty()) {
                throw new ResourceNotFoundException("Post o id " + postId + " nie istnieje");
            }
            return post.get();
        }

        @PostMapping(path = "/authorized/admin/posts/add")
        public Map<String, Object> addPost(@RequestBody Post post) {
            return postService.addPost(post);
        }

        @DeleteMapping(path = "/authorized/admin/posts/delete/{postId}")
        public Map<String, Object> deletePost(@PathVariable("postId") Long postId) {
            return postService.deletePost(postId);
        }

        @PutMapping(path = "/authorized/admin/posts/update/{postId}")
        public Map<String, Object> updatePost(@PathVariable("postId") Long postId, @RequestBody Post post) {
            return postService.updatePost(postId, post);
        }
}
