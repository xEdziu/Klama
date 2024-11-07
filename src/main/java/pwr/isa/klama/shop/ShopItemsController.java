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

    @GetMapping(path = "/authorized/shopItems/history")
    public List<PurchaseRecordDTO> getPurchaseHistory() {
        return ShopItemsService.getPurchaseHistory();
    }

    @PostMapping(path = "/authorized/shopItems/buy")
    public Map<String, Object> buyShopItems(@RequestBody List<PurchaseRequest> purchaseRequests) {
        return ShopItemsService.buyShopItems(purchaseRequests);
    }

    // ============ Admin methods ============

    @GetMapping(path = "/authorized/admin/shopItems/history")
    public List<PurchaseRecordDTO> getPurchaseHistoryAll() {
        return ShopItemsService.getPurchaseHistoryAll();
    }

    @GetMapping(path = "/authorized/admin/shopItems")
    public List<ShopItems> getShopItemsAll() {
        return ShopItemsService.getShopItemsAll();
    }

    @PostMapping(path = "/authorized/admin/shopItems/add")
    public Map<String, Object> addShopItem(@RequestBody ShopItems shopItems) {
        return ShopItemsService.addShopItem(shopItems);
    }

    @PutMapping(path = "/authorized/admin/shopItems/update/{id}")
    public Map<String, Object> updateShopItem(@PathVariable("id") Long id,
                                              @RequestBody ShopItems shopItems) {
        return ShopItemsService.updateShopItem(id, shopItems);
    }

    @DeleteMapping(path = "/authorized/admin/shopItems/delete/{id}")
    public Map<String, Object> deleteShopItem(@PathVariable("id") Long id) {
        return ShopItemsService.deleteShopItem(id);
    }
}
