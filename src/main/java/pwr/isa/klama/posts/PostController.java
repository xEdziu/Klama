package pwr.isa.klama.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return postService.getPosts();
        }
}
