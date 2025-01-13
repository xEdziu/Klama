package pwr.isa.klama.shop;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.pass.Pass;
import pwr.isa.klama.pass.userPass.UserPass;
import pwr.isa.klama.pass.userPass.UserPassStatus;
import pwr.isa.klama.security.logging.ApiLogger;
import pwr.isa.klama.shop.purchase.*;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        ApiLogger.logInfo("/api/v1/shopItems", "Get all active shop items");
        return shopItemsRepository.findByActive(true);
    }

    public ShopItems getShopItemById(Long id) {
        ApiLogger.logInfo("/api/v1/shopItems/" + id, "Get shop item by id");
        ShopItems shopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o  " + id + " nie istnieje w sklepie"));

        if (!shopItem.getActive())
            throw new ResourceNotFoundException("Przedmiot o  " + id + " nie jest dostępny w sklepie");

        return shopItem;
    }

    public List<ShopItems> getShopItemsAll() {
        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems", "Get all shop items");
        return shopItemsRepository.findAll();
    }

    @Transactional
    public Map<String, Object> buyShopItems(List<PurchaseRequest> purchaseRequests) {
        ApiLogger.logInfo("/api/v1/authorized/shopItems/buy", "Buy shop items - user " + getCurrentUser().getId());
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

        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems/add", "Add shop item - admin " + getCurrentUser().getId());

        if (shopItemsRepository.findByName(shopItems.getName()).isPresent()) {
            throw new IllegalStateException("Przedmiot o nazwie " + shopItems.getName() + " już istnieje w sklepie");
        }

        if (shopItemsRepository.findByDescription(shopItems.getDescription()).isPresent()) {
            throw new IllegalStateException("Przedmiot o opisie " + shopItems.getDescription() + " już istnieje w sklepie");
        }

        // Check if the quantity is valid
        if (shopItems.getQuantity() < 0) {
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

        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems/update/" + id, "Update shop item - admin " + getCurrentUser().getId());

        ShopItems existingShopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + id + " nie istnieje w sklepie"));

        if (shopItems.getQuantity() != null && shopItems.getQuantity() < 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/shopItems/update/" + id, "Quantity cannot be less than 0");
            throw new IllegalStateException("Ilość nie może być mniejsza niż 0");
        }

        if (shopItems.getPrice() != null && shopItems.getPrice() <= 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/shopItems/update/" + id, "Price cannot be less than or equal to 0");
            throw new IllegalStateException("Cena nie może być mniejsza lub równa 0");
        }

        // Update fields if they are provided
        if (shopItems.getName() != null && !shopItems.getName().isEmpty()) {
            existingShopItem.setName(shopItems.getName());
        }
        if (shopItems.getDescription() != null && !shopItems.getDescription().isEmpty()) {
            existingShopItem.setDescription(shopItems.getDescription());
        }
        if (shopItems.getPrice() != null && shopItems.getPrice() > 0) {
            existingShopItem.setPrice(shopItems.getPrice());
        }
        if (shopItems.getQuantity() != null && shopItems.getQuantity() >= 0) {
            existingShopItem.setQuantity(shopItems.getQuantity());
        }
        if (shopItems.getActive() != null) {
            existingShopItem.setActive(shopItems.getActive());
        }

        shopItemsRepository.save(existingShopItem);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deleteShopItem(Long id) {

        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems/delete/" + id, "Delete shop item - admin " + getCurrentUser().getId());

        ShopItems shopItem = shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + id + " nie istnieje w sklepie"));

        String message;
        // Check if the item is associated with any purchase
        if (purchaseRepository.existsByItems_ShopItem_Id(id)) {
            ApiLogger.logWarning("/api/v1/authorized/admin/shopItems/delete/" + id, "Przedmiot jest powiązany z zakupem, deaktywacja przedmiotu");
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

        ApiLogger.logInfo("/api/v1/authorized/user/purchaseHistory", "Get purchase history - user " + getCurrentUser().getId());

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

        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems/history", "Get purchase history - admin " + getCurrentUser().getId());

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

    public List<PurchaseRecordDTO> getPurchaseHistoryByUserId(Long userId) {
        ApiLogger.logInfo("/api/v1/authorized/admin/shopItems/history/" + userId, "Getting purchase history for user: " + userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Użytkownik o id " + userId + " nie istnieje");
        }

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

    public Map<String, Object> generateUserPurchase() {
        User user = userRepository.findById(1L).orElseThrow(() -> new NoSuchElementException("User not found"));
        ShopItems shopItem = shopItemsRepository.findById(1L).orElseThrow(() -> new NoSuchElementException("Shop item not found"));

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            LocalDateTime purchaseDate = now.minusDays(i);

            Random rand = new Random();
            int min = 3;
            int max = 20;
            int x = rand.nextInt((max - min) + 1) + min;

            for (int j = 0; j < x; j++) {
                Purchase purchase = new Purchase(user, Timestamp.valueOf(purchaseDate), shopItem.getPrice() * x, new ArrayList<>());
                PurchaseItem purchaseItem = new PurchaseItem(purchase, shopItem, x, shopItem.getPrice());
                purchaseItem.setPurchase(purchase);
                purchaseRepository.save(purchase);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Wygenerowano zakupy dla usera o id 1");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        return response;
    }
}
