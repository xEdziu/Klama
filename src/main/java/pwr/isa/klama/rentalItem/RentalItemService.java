package pwr.isa.klama.rentalItem;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.rentalItem.rent.*;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RentalItemService {

    private final RentalItemRepository rentalItemRepository;
    private final UserRepository userRepository;
    private final RentRepository rentRepository;

    @Autowired
    public RentalItemService(RentalItemRepository rentalItemRepository, UserRepository userRepository, RentRepository rentRepository) {
        this.rentalItemRepository = rentalItemRepository;
        this.userRepository = userRepository;
        this.rentRepository = rentRepository;
    }

    public List<RentalItem> getRentalItem() {return rentalItemRepository.findAll(); }

    public RentalItem getRentalItemById(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new IllegalArgumentException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        return rentalItemRepository.findById(rentalItemId).orElse(null);
    }

    public Map<String, Object> addNewRentalItem(RentalItem rentalItem) {
        Optional<RentalItem> rentalItemOptional = rentalItemRepository.findRentalItemByName(rentalItem.getName());
        if (rentalItemOptional.isPresent()) {
            throw new IllegalArgumentException("Przedmiot o nazwie: " + rentalItem.getName() + " już istnieje");
        }
        rentalItemRepository.save(rentalItem);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot został dodany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deleteRentalItem(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new IllegalArgumentException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        rentalItemRepository.deleteById(rentalItemId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot o id " + rentalItemId + " został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> updateRentalItem(Long rentalItemId,
                                                RentalItem rentalItem) {
        if(rentalItem.getName() == null &&
                rentalItem.getId() == null &&
                rentalItem.getPrice() == null &&
                rentalItem.getQuantity() == null) {
            throw new IllegalArgumentException("Należy podać jeden z parametrów");
        }
        if(!rentalItemRepository.existsById(rentalItemId)) {
            throw new IllegalArgumentException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }

        RentalItem rentalItemToUpdate = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalStateException("Przedmiot o id " + rentalItemId + " nie istnieje"));

        if(rentalItem.getName() != null) {
            if(rentalItemToUpdate.getName().isEmpty()) {
                throw new IllegalStateException("Nazwa nie może być pusta");
            }
            rentalItemToUpdate.setName(rentalItem.getName());
        }

        if(rentalItem.getPrice() != null) {
            if(rentalItemToUpdate.getPrice() == null) {
                throw new IllegalStateException("Cena nie możę być pusta");
            }
            rentalItemToUpdate.setPrice(rentalItem.getPrice());
        }

        if(rentalItem.getQuantity() != null) {
            if(rentalItemToUpdate.getQuantity() == null) {
                throw new IllegalStateException("Ilość nie może być pusta");
            }
            rentalItemToUpdate.setQuantity(rentalItem.getQuantity());
        }

        if(rentalItem.getDescription() != null) {
            if(rentalItemToUpdate.getDescription().isEmpty()) {
                throw new IllegalStateException("Opis nie może być pusty");
            }
            rentalItemToUpdate.setDescription(rentalItem.getDescription());
        }

        rentalItemRepository.save(rentalItemToUpdate);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> rentRentalItems(List<RentRequest> rentRequests) {
        Map<String, Object> response = new HashMap<>();
        float totalRentPrice = 0;
        List<RentItem> rentItems = new ArrayList<>();
        
        for (RentRequest request : rentRequests) {
            RentalItem rentalItem = rentalItemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + request.getItemId() + " nie istnieje w wypożyczlni"));

            // Check if the quantity is valid
            if (request.getQuantity() <= 0) {
                throw new IllegalStateException("Niepoprawna ilość przedmiotów");
            }

            // Check if the quantity is available
            if (rentalItem.getQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Brak wystarczającej ilości przedmiotów");
            }

            // Update the quantity
            rentalItem.setQuantity(rentalItem.getQuantity() - request.getQuantity());
            rentalItemRepository.save(rentalItem);
            
            float itemTotalPrice = rentalItem.getPrice() * request.getQuantity();
            totalRentPrice += itemTotalPrice;

            RentItem rentItem = new RentItem();
            rentItem.setRentalItem(rentalItem);
            rentItem.setQuantity(request.getQuantity());
            rentItem.setPrice(rentalItem.getPrice());
            rentItem.setTotalPrice(itemTotalPrice);
            rentItems.add(rentItem);

        }

        // Two decimal places for total purchase price
        totalRentPrice = (float) (Math.round(totalRentPrice * 100.0) / 100.0);

        // Create a new rent
        Rent rent = new Rent();
        rent.setUser(getCurrentUser()); // Assuming user is retrieved from the security context
        //TODO: Dostosować date wyporzyczania i zwrotu
        rent.setRentDate(new Timestamp(new Date().getTime()));
        rent.setReturnDate(new Timestamp(new Date().getTime()));
        rent.setTotalPrice(totalRentPrice);
        rent.setItems(rentItems);

        for (RentItem item : rentItems) {
            item.setRent(rent);
        }

        rentRepository.save(rent);

        response.put("message", "Wypożycznie zakończone sukcesem");
        response.put("totalPrice", totalRentPrice);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    private User getCurrentUser() {
        //TODO: temporary solution before implementing Logging in
        // Implementacja pobierania ID aktualnie zalogowanego użytkownika
        // Może to być np. z kontekstu bezpieczeństwa Spring Security
        // return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId();
        return userRepository.findById(1L).orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika o Id 1")); // Tymczasowe rozwiązanie
    }

    public List<RentRecordDTO> getRentHistory() {
        Long userId = getCurrentUser().getId();
        List<Rent> rents = rentRepository.findByUserId(userId);
        List<RentRecordDTO> rentRecords = new ArrayList<>();

        for (Rent rent : rents) {
            List<RentDTO> items = new ArrayList<>();
            for (RentItem item : rent.getItems()) {
                RentDTO dto = new RentDTO(
                        item.getRentalItem().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice()
                );
                items.add(dto);
            }
            rentRecords.add(new RentRecordDTO(
                    rent.getId(),
                    userId,
                    rent.getRentDate(),
                    rent.getReturnDate(),
                    items,
                    rent.getTotalPrice()
            ));
        }

        return rentRecords;
    }

    public List<RentRecordDTO> getRentHistoryAll() {
        List<Rent> rents = rentRepository.findAll();
        List<RentRecordDTO> rentRecords = new ArrayList<>();

        for (Rent rent : rents) {
            List<RentDTO> items = new ArrayList<>();
            for (RentItem item : rent.getItems()) {
                RentDTO dto = new RentDTO(
                        item.getRentalItem().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice()
                );
                items.add(dto);
            }
            rentRecords.add(new RentRecordDTO(
                    rent.getId(),
                    rent.getUser().getId(),
                    rent.getRentDate(),
                    rent.getReturnDate(),
                    items,
                    rent.getTotalPrice()
            ));
        }

        return rentRecords;
    }


}

