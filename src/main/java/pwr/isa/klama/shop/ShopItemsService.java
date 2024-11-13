package pwr.isa.klama.shop;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.shop.purchase.*;
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
        return shopItemsRepository.findByActive(true);
    }

    public ShopItems getShopItemById(Long id) {
        ShopItems shopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o  " + id + " nie istnieje w sklepie"));

        if (!shopItem.getActive())
            throw new ResourceNotFoundException("Przedmiot o  " + id + " nie jest dostępny w sklepie");

        return shopItem;
    }

    public List<ShopItems> getShopItemsAll() {
        return shopItemsRepository.findAll();
    }

    @Transactional
    public Map<String, Object> buyShopItems(List<PurchaseRequest> purchaseRequests) {
        Map<String, Object> response = new HashMap<>();
        float totalPurchasePrice = 0;
        List<PurchaseItem> purchaseItems = new ArrayList<>();

        for (PurchaseRequest request : purchaseRequests) {
            ShopItems shopItem = shopItemsRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + request.getItemId() + " nie istnieje w sklepie"));

            if (request.getQuantity() <= 0) {
                throw new IllegalStateException("Niepoprawna ilość przedmiotów");
            }

            if (shopItem.getQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Brak wystarczającej ilości przedmiotów");
            }

            shopItem.setQuantity(shopItem.getQuantity() - request.getQuantity());
            shopItemsRepository.save(shopItem);

            float itemTotalPrice = shopItem.getPrice() * request.getQuantity();
            totalPurchasePrice += itemTotalPrice;

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setShopItem(shopItem);
            purchaseItem.setQuantity(request.getQuantity());
            purchaseItem.setPrice(shopItem.getPrice());
            purchaseItem.setTotalPrice(itemTotalPrice);
            purchaseItems.add(purchaseItem);
        }

        //two decimal places for total purchase price
        totalPurchasePrice = (float) (Math.round(totalPurchasePrice * 100.0) / 100.0);

        Purchase purchase = new Purchase();
        purchase.setUser(getCurrentUser());
        purchase.setPurchaseDate(new Timestamp(new Date().getTime()));
        purchase.setTotalPrice(totalPurchasePrice);
        purchase.setItems(purchaseItems);

        for (PurchaseItem item : purchaseItems) {
            item.setPurchase(purchase);
        }

        purchaseRepository.save(purchase);

        response.put("message", "Zakup zakończony sukcesem");
        response.put("totalPrice", totalPurchasePrice);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Map<String, Object> addShopItem(ShopItems shopItems) {

        if (shopItemsRepository.findByName(shopItems.getName()).isPresent()) {
            throw new IllegalStateException("Przedmiot o nazwie " + shopItems.getName() + " już istnieje w sklepie");
        }

        if (shopItemsRepository.findByDescription(shopItems.getDescription()).isPresent()) {
            throw new IllegalStateException("Przedmiot o opisie " + shopItems.getDescription() + " już istnieje w sklepie");
        }

        // Check if the quantity is valid
        if (shopItems.getQuantity() <= 0) {
            throw new IllegalStateException("Niepoprawna ilość przedmiotów");
        }

        // Check if the price is valid
        if (shopItems.getPrice() <= 0) {
            throw new IllegalStateException("Niepoprawna cena przedmiotu");
        }

        // Check if the name is valid
        if (shopItems.getName().isEmpty()) {
            throw new IllegalStateException("Niepoprawna nazwa przedmiotu");
        }

        // Check if the description is valid
        if (shopItems.getDescription().isEmpty()) {
            throw new IllegalStateException("Niepoprawny opis przedmiotu");
        }

        Map<String, Object> response = new HashMap<>();
        shopItemsRepository.save(shopItems);
        response.put("message", "Przedmiot dodany do sklepu");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> updateShopItem(Long id, ShopItems shopItems) {
        ShopItems shopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + id + " nie istnieje w sklepie"));

        // check if name or description is valid and present
        if (shopItems.getName().isEmpty() || shopItems.getDescription().isEmpty()) {
            throw new IllegalStateException("Niepoprawna nazwa lub opis przedmiotu");
        }

        // check if price is valid
        if (shopItems.getPrice() <= 0) {
            throw new IllegalStateException("Niepoprawna cena przedmiotu");
        }

        // check if quantity is valid
        if (shopItems.getQuantity() <= 0) {
            throw new IllegalStateException("Niepoprawna ilość przedmiotów");
        }

        Map<String, Object> response = new HashMap<>();
        shopItem.setName(shopItems.getName());
        shopItem.setDescription(shopItems.getDescription());
        shopItem.setPrice(shopItems.getPrice());
        shopItem.setQuantity(shopItems.getQuantity());
        shopItemsRepository.save(shopItem);

        response.put("message", "Przedmiot zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deleteShopItem(Long id) {
        ShopItems shopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + id + " nie istnieje w sklepie"));

        String message;
        // Check if the item is associated with any purchase
        if (purchaseRepository.existsByItems_ShopItem_Id(id)) {
            message = "Przedmiot jest powiązany z zakupem, deaktywacja przedmiotu";
            shopItem.setActive(false);
            shopItemsRepository.save(shopItem);
        } else {
            message = "Przedmiot usunięty";
            shopItemsRepository.delete(shopItem);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<PurchaseRecordDTO> getPurchaseHistory() {
        Long userId = getCurrentUser().getId();
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        List<PurchaseRecordDTO> purchaseRecords = new ArrayList<>();

        for (Purchase purchase : purchases) {
            List<PurchaseDTO> items = new ArrayList<>();
            for (PurchaseItem item : purchase.getItems()) {
                PurchaseDTO dto = new PurchaseDTO(
                        item.getShopItem().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice()
                );
                items.add(dto);
            }
            purchaseRecords.add(new PurchaseRecordDTO(
                    purchase.getId(),
                    userId,
                    purchase.getPurchaseDate(),
                    items,
                    purchase.getTotalPrice()
            ));
        }

        return purchaseRecords;
    }

    public List<PurchaseRecordDTO> getPurchaseHistoryAll() {
        List<Purchase> purchases = purchaseRepository.findAll();
        List<PurchaseRecordDTO> purchaseRecords = new ArrayList<>();

        for (Purchase purchase : purchases) {
            List<PurchaseDTO> items = new ArrayList<>();
            for (PurchaseItem item : purchase.getItems()) {
                PurchaseDTO dto = new PurchaseDTO(
                        item.getShopItem().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice()
                );
                items.add(dto);
            }
            purchaseRecords.add(new PurchaseRecordDTO(
                    purchase.getId(),
                    purchase.getUser().getId(),
                    purchase.getPurchaseDate(),
                    items,
                    purchase.getTotalPrice()
            ));
        }

        return purchaseRecords;
    }
}
