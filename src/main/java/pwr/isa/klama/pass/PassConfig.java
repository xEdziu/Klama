package pwr.isa.klama.pass;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PassConfig {

    @Bean
    CommandLineRunner commandLineRunnerForPasses(PassRepository repository) {
        return args -> {
            Pass monthlyPass = new Pass(
                    "Monthly Pass",
                    "Access for one month",
                    100.00F,
                    30,
                    PassStatus.VISIBLE
            );

            Pass weeklyPass = new Pass(
                    "Weekly Pass",
                    "Access for one week",
                    30.00F,
                    7,
                    PassStatus.VISIBLE
            );

            Pass dailyPass = new Pass(
                    "Daily Pass",
                    "Access for one day",
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
