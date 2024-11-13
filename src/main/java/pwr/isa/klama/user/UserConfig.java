package pwr.isa.klama.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pwr.isa.klama.security.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunnerForUsers(UserRepository userRepository) {
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
        };
    }
}
