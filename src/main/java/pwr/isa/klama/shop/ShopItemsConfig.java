package pwr.isa.klama.shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pwr.isa.klama.pass.userPass.UserPass;
import pwr.isa.klama.pass.userPass.UserPassStatus;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Configuration
public class ShopItemsConfig {

    @Bean
    CommandLineRunner commandLineRunnerForShopItems(
            ShopItemsRepository repository) {
        return args -> {
            ShopItems magnezja = new ShopItems(
                    "Magnezja 100g Black Diamond",
                    "Substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
                    25.99F,
                    100
            );

            ShopItems magnezja2 = new ShopItems(
                    "Magnezja 500g Black Diamond",
                    "Substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
                    119.99F,
                    100
            );

            ShopItems karabinek = new ShopItems(
                    "Karabinek Black Diamond",
                    "Karabinek wspinaczkowy o wysokiej wytrzymałości",
                    45.99F,
                    50
            );
            ShopItems lina = new ShopItems(
                    "Lina wspinaczkowa 60m",
                    "Lina dynamiczna do wspinaczki o długości 60 metrów",
                    599.99F,
                    10
            );

            ShopItems buty = new ShopItems(
                    "LaSportiva Solution 42",
                    "Buty przeznaczone do wspinaczki zapewniające bezpieczne i ułatwione doświadczenie wspinania",
                    349.99F,
                    15
            );

            repository.saveAll(
                    List.of(magnezja, buty, karabinek, lina)
            );
        };
    }
}
