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
        return shopItemsRepository.findByActive(true);
    }

    public ShopItems getShopItemById(Long id) {
        return shopItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o  " + id + " nie istnieje w sklepie"));
    }

    public List<ShopItems> getShopItemsAll() {
        return shopItemsRepository.findAll();
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

    public Map<String, Object> addShopItem(ShopItems shopItems) {

        // Check if the item already exists
        if (shopItemsRepository.existsById(shopItems.getId())) {
            throw new IllegalStateException("Przedmiot o id " + shopItems.getId() + " już istnieje w sklepie");
        }

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
        // Sprawdź, czy przedmiot jest powiązany z jakimkolwiek zakupem
        if (purchaseRepository.existsByShopItemId(id)) {
            message = "Przedmiot jest powiązany z zakupem, deaktywacja przedmiotu";
            shopItem.setActive(false);
            shopItemsRepository.save(shopItem);
        } else {
            message = "Przedmiot usunięty";
            shopItemsRepository.delete(shopItem);
        }

        Map<String, Object> response = new HashMap<>();
        shopItemsRepository.delete(shopItem);
        response.put("message", message);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<PurchaseRecordDTO> getPurchaseHistory() {
        Long userId = getCurrentUserId();
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        Map<Long, List<PurchaseDTO>> purchaseMap = new HashMap<>();
        Map<Long, Date> purchaseDateMap = new HashMap<>();

        for (Purchase purchase : purchases) {
            PurchaseDTO dto = new PurchaseDTO(
                    purchase.getShopItem().getName(),
                    purchase.getQuantity(),
                    purchase.getTotalPrice(),
                    purchase.getShopItem().getPrice()
            );

            purchaseMap.computeIfAbsent(purchase.getId(), k -> new ArrayList<>()).add(dto);
            purchaseDateMap.put(purchase.getId(), purchase.getPurchaseDate());
        }

        List<PurchaseRecordDTO> purchaseRecords = new ArrayList<>();
        for (Map.Entry<Long, List<PurchaseDTO>> entry : purchaseMap.entrySet()) {
            double totalAmount = entry.getValue().stream().mapToDouble(PurchaseDTO::getTotalPrice).sum();
            Date purchaseDate = purchaseDateMap.get(entry.getKey());
            purchaseRecords.add(new PurchaseRecordDTO(entry.getKey(), userId, purchaseDate, entry.getValue(), (float) totalAmount));
        }

        return purchaseRecords;
    }

    private Long getCurrentUserId() {
        // Implementacja pobierania ID aktualnie zalogowanego użytkownika
        // Może to być np. z kontekstu bezpieczeństwa Spring Security
        // return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId();
        return 1L; // Tymczasowe rozwiązanie
    }

    public List<PurchaseRecordDTO> getPurchaseHistoryAll() {
        List<Purchase> purchases = purchaseRepository.findAll();
        Map<Long, List<PurchaseDTO>> purchaseMap = new HashMap<>();
        Map<Long, Long> purchaseUserMap = new HashMap<>();
        Map<Long, Date> purchaseDateMap = new HashMap<>();

        for (Purchase purchase : purchases) {
            PurchaseDTO dto = new PurchaseDTO(
                    purchase.getShopItem().getName(),
                    purchase.getQuantity(),
                    purchase.getTotalPrice(),
                    purchase.getShopItem().getPrice()
            );

            purchaseMap.computeIfAbsent(purchase.getId(), k -> new ArrayList<>()).add(dto);
            purchaseUserMap.put(purchase.getId(), purchase.getUser().getId());
            purchaseDateMap.put(purchase.getId(), purchase.getPurchaseDate());
        }

        List<PurchaseRecordDTO> purchaseRecords = new ArrayList<>();
        for (Map.Entry<Long, List<PurchaseDTO>> entry : purchaseMap.entrySet()) {
            double totalAmount = entry.getValue().stream().mapToDouble(PurchaseDTO::getTotalPrice).sum();
            Long userId = purchaseUserMap.get(entry.getKey());
            Date purchaseDate = purchaseDateMap.get(entry.getKey());
            purchaseRecords.add(new PurchaseRecordDTO(entry.getKey(), userId, purchaseDate, entry.getValue(), (float) totalAmount));
        }

        return purchaseRecords;
    }
}
