package pwr.isa.klama.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pwr.isa.klama.posts.Post;
import pwr.isa.klama.posts.PostRepository;
import pwr.isa.klama.security.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunnerForUsers(UserRepository userRepository, PostRepository postRepository) {
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        BCryptPasswordEncoder bCryptPasswordEncoder = passwordEncoder.bCryptPasswordEncoder();
        return args -> {
            User admin = new User(
                    "Admin",
                    "Default",
                    "Admin",
                    "default.admin@gmail.com",
                    bCryptPasswordEncoder.encode(String.valueOf(UUID.randomUUID())),
                    UserRole.ROLE_ADMIN,
                    new Timestamp(new Date().getTime())
            );
            userRepository.save(admin);
            userRepository.enableUser(admin.getEmail());
            String password = "superSecretPassword";
            User sampleAdmin = new User(
                    "Sample",
                    "Sample",
                    "Admin",
                    "sampleAdmin@mail.com",
                    bCryptPasswordEncoder.encode(password),
                    UserRole.ROLE_ADMIN,
                    new Timestamp(new Date().getTime())
            );
            userRepository.save(sampleAdmin);
            userRepository.enableUser(sampleAdmin.getEmail());
            String userPassword = "agoral06";
            User user = new User(
                    "eddy06",
                    "Adrian",
                    "Goral",
                    "adrian.goral@gmail.com",
                    bCryptPasswordEncoder.encode(userPassword),
                    UserRole.ROLE_USER,
                    new Timestamp(new Date().getTime())
            );
            userRepository.save(user);
            userRepository.enableUser(user.getEmail());
            Post post = new Post(
                    "Sample post",
                    "Sample post content",
                    admin,
                    new Timestamp(new Date().getTime()),
                    new Timestamp(new Date().getTime())
            );
            postRepository.save(post);
            Post post2 = new Post(
                    "Sample post 2",
                    "Sample post content 2",
                    admin,
                    new Timestamp(new Date().getTime()),
                    new Timestamp(new Date().getTime())
            );
            postRepository.save(post2);
            Post post3 = new Post(
                    "Sample post 3",
                    "Sample post content 3",
                    admin,
                    new Timestamp(new Date().getTime()),
                    new Timestamp(new Date().getTime())
            );
            postRepository.save(post3);
            Post post4 = new Post(
                    "Sample post 4",
                    "Sample post content 4",
                    admin,
                    new Timestamp(new Date().getTime()),
                    new Timestamp(new Date().getTime())
            );
            postRepository.save(post4);
        };
    }
}
