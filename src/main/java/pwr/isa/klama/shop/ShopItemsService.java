package pwr.isa.klama.shop;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
public class ShopItemsService {

    private final ShopItemsRepository shopItemsRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public ShopItemsService(ShopItemsRepository shopItemsRepository, UserRepository userRepository, PurchaseRepository purchaseRepository) {
        this.shopItemsRepository = shopItemsRepository;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public List<ShopItems> getShopItems() {
        return shopItemsRepository.findAll();
    }

    public ShopItems getShopItemById(Long id) {
        return shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o  " + id + " nie istnieje w sklepie"));
    }

    @Transactional
    public Map<String, Object> buyShopItems(List<PurchaseRequest> purchaseRequests) {
        Map<String, Object> response = new HashMap<>();
        for (PurchaseRequest request : purchaseRequests) {
            System.out.println(request);
            ShopItems shopItem = shopItemsRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + request.getItemId() + " nie istnieje w sklepie"));

            // Check if the quantity is valid
            if (request.getQuantity() <= 0) {
                throw new IllegalStateException("Niepoprawna ilość przedmiotów");
            }

            //TODO: temporary solution before implementing Logging in
            Optional<User> tmpAdmin = userRepository.findById(1L);

            if (tmpAdmin.isEmpty())
                throw new ResourceNotFoundException("Nie znaleziono użytkownika o Id 1");

            // Check if the quantity is available
            if (shopItem.getQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Brak wystarczającej ilości przedmiotów");
            }

            // Update the quantity
            shopItem.setQuantity(shopItem.getQuantity() - request.getQuantity());
            shopItemsRepository.save(shopItem);

            // Create a new purchase
            Purchase purchase = new Purchase();
            purchase.setUser(tmpAdmin.get()); // Assuming user is retrieved from the security context
            purchase.setShopItem(shopItem);
            purchase.setQuantity(request.getQuantity());
            purchase.setPurchaseDate(new Timestamp(new Date().getTime()));
            purchase.setTotalPrice(shopItem.getPrice() * request.getQuantity());
            purchaseRepository.save(purchase);
        }

        response.put("message", "Zakup zakończony sukcesem");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }
}
