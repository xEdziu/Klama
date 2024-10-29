package pwr.isa.klama.shop_items;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class ShopItemsController {
    private final ShopItemsService shopItemsService;
    @Autowired
    public ShopItemsController(ShopItemsService shopItemsService) {
        this.shopItemsService = shopItemsService;
    }

    @GetMapping(path = "/shopItems")
//    public List<ShopItems> getShopItems() {
//        return shopItemsService.getShopItems();
//    }
    public String getShopItems() {
        return "hello";
    }
}
