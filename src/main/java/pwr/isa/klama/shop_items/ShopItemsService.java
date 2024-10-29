package pwr.isa.klama.shop_items;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopItemsService {
    public List<ShopItems> getShopItems() {
        return List.of(
                new ShopItems(
                        1,
                        "magnezja",
                        "substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
                        25.99F,
                        100
                )
        );
    }
}
