package pwr.isa.klama.shop_items;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1")
public class ShopItemsController {
    private final ShopItemsService ShopItemsService;
    @Autowired
    public ShopItemsController(ShopItemsService ShopItemsService) {
        this.ShopItemsService = ShopItemsService;
    }

    @GetMapping(path = "/shopItems")
    public List<ShopItems> getShopItems() {
        return ShopItemsService.getShopItems();
    }
//    public String getShopItems() {
//        return "hello";
//    }
}
