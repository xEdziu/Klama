package pwr.isa.klama.shop_items;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopItemsService {

    private final ShopItemsRepository shopItemsRepository;

    @Autowired
    public ShopItemsService(ShopItemsRepository shopItemsRepository) {
        this.shopItemsRepository = shopItemsRepository;
    }

    public List<ShopItems> getShopItems() {
        return shopItemsRepository.findAll();
    }

//    public List<ShopItems> getShopItems() {
//        return List.of(
//                new ShopItems(
//                        1,
//                        "magnezja",
//                        "substancja zwiększająca tarcie i umożliwianie pewne chwytanie przedmiotów",
//                        25.99F,
//                        100
//                )
//        );
//    }
}
