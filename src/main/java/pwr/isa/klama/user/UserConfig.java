package pwr.isa.klama.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pwr.isa.klama.security.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Date;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository) {
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        BCryptPasswordEncoder bCryptPasswordEncoder = passwordEncoder.bCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode("Adrian-Admin-2025");
        return args -> {
            User admin = new User(
                    "eddy06",
                    "Adrian",
                    "Goral",
                    "adrian.goral@gmail.com",
                    password,
                    UserRole.ADMIN,
                    new Timestamp(new Date().getTime()),
                    null
            );
            userRepository.save(admin);
            userRepository.enableUser(admin.getEmail());
        };
    }
}
