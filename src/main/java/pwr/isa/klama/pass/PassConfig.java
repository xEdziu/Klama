package pwr.isa.klama.pass;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import pwr.isa.klama.pass.userPass.UserPass;
import pwr.isa.klama.pass.userPass.UserPassRepository;
import pwr.isa.klama.pass.userPass.UserPassStatus;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Configuration
@DependsOn({"userRepository", "userConfig"})
public class PassConfig {

    @Bean
    CommandLineRunner commandLineRunnerForPasses(PassRepository repository, UserPassRepository userPassRepository, UserRepository userRepository) {
        return args -> {
            Pass monthlyPass = new Pass(
                    "Karnet miesięczny",
                    "Nielimitowany dostęp na 30 dni",
                    100.00F,
                    30,
                    PassStatus.VISIBLE
            );

            Pass weeklyPass = new Pass(
                    "Karnet tygodniowy",
                    "Nielimitowany dostęp na 7 dni",
                    30.00F,
                    7,
                    PassStatus.VISIBLE
            );

            Pass dailyPass = new Pass(
                    "Karnet dzienny",
                    "Nielimitowany dostęp na 1 dzień",
                    5.00F,
                    1,
                    PassStatus.VISIBLE
            );

            repository.saveAll(
                    List.of(monthlyPass, weeklyPass, dailyPass)
            );
        };
    }
}
