package pwr.isa.klama.shop_items;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CommandLinePropertySource;

import java.util.List;

@Configuration
public class ShopItemsConfig {

    @Bean
    CommandLineRunner commandLineRunner(
            ShopItemsRepository repository){
        return args -> {
            ShopItems magnezja = new ShopItems(
                    1,
                    "magnezja",
                    "substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
                    25.99F,
                    100
            );

            ShopItems buty = new ShopItems(
                    "buty",
                    "buty przeznaczone do wspinaczki zapewniające bezpieczne i ułatwione doświadczenie wspinania",
                    349.99F,
                    50
            );

            repository.saveAll(
                    List.of(magnezja, buty)
            );
        };
    }
}
