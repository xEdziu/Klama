package pwr.isa.klama.rentalItem;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RentalItemConfig {

    @Bean
    CommandLineRunner commandLineRunnerForRentalItems( RentalItemRepository repository) {
        return args -> {
            RentalItem magnezja = new RentalItem(
                    "magnezja",
                    "opis jakis tam",
                    15.5F,
                    30
            );

            RentalItem buty = new RentalItem(
                    "buty",
                    "opis jakis tam",
                    18.5F,
                    15
            );

            repository.saveAll(
                    List.of(magnezja,buty)
            );
        };
    }
}
