package pwr.isa.klama.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1")
public class ShopItemsController {

    private final ShopItemsService ShopItemsService;

    @Autowired
    public ShopItemsController(ShopItemsService ShopItemsService) {
        this.ShopItemsService = ShopItemsService;
    }

    // ============ Freely available methods ============
    @GetMapping(path = "/shopItems")
    public List<ShopItems> getShopItems() {
        return ShopItemsService.getShopItems();
    }

    @GetMapping(path = "/shopItems/{id}")
    public ShopItems getShopItemById(@PathVariable("id") Long id) {
        return ShopItemsService.getShopItemById(id);
    }

    // ============ User methods ============

    @PostMapping(path = "/authorized/shopItems/buy")
    public Map<String, Object> buyShopItems(@RequestBody List<PurchaseRequest> purchaseRequests) {
        return ShopItemsService.buyShopItems(purchaseRequests);
    }

    // ============ Admin methods ============

}
