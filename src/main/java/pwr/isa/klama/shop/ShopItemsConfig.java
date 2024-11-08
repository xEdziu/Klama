package pwr.isa.klama.shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ShopItemsConfig {

    @Bean
    CommandLineRunner commandLineRunnerForShopItems(
            ShopItemsRepository repository){
        return args -> {
            ShopItems magnezja = new ShopItems(
                    "Magnezja 100g Black Diamond",
                    "Substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
                    25.99F,
                    100
            );

            ShopItems buty = new ShopItems(
                    "LaSportiva Solution 42",
                    "Buty przeznaczone do wspinaczki zapewniające bezpieczne i ułatwione doświadczenie wspinania",
                    349.99F,
                    15
            );

            repository.saveAll(
                    List.of(magnezja, buty)
            );
        };
    }
}
