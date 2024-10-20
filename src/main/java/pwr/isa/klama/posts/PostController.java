package pwr.isa.klama.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/post")
public class PostController {

        private final PostService postService;

        @Autowired
        public PostController(PostService postService) {
                this.postService = postService;
        }

        @GetMapping
        public List<Post> getPosts() {
            return postService.getAllPosts();
        }

        @PostMapping(path = "/add")
        public void addPost(@RequestBody Post post) {
            postService.addPost(post);
        }

        @DeleteMapping(path = "/delete/{postId}")
        public void deletePost(@PathVariable("postId") Long postId) {
            postService.deletePost(postId);
        }

        @PutMapping(path = "/update/{postId}")
        public void updatePost(@PathVariable("postId") Long postId, @RequestBody Post post) {
            postService.updatePost(postId, post);
        }
}
