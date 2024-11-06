package pwr.isa.klama.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pwr.isa.klama.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findPostByTitle(String title);

    List<Post> findAllByAuthorId(User authorId);
}
