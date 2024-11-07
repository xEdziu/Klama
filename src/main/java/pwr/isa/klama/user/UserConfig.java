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
                    UserRole.ADMIN,
                    new Timestamp(new Date().getTime()),
                    null
            );
            userRepository.save(admin);
            userRepository.enableUser(admin.getEmail());
            User sampleAdmin = new User(
                    "Sample",
                    "Sample",
                    "Admin",
                    "sampleAdmin@mail.com",
                    bCryptPasswordEncoder.encode(String.valueOf(UUID.randomUUID())),
                    UserRole.ADMIN,
                    new Timestamp(new Date().getTime()),
                    null
            );
            userRepository.save(sampleAdmin);
            userRepository.enableUser(sampleAdmin.getEmail());
        };
    }
}
