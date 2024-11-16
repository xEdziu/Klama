package pwr.isa.klama.rentalItem;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public List<RentalItem> getRentalItemsForAdmin() {return rentalItemRepository.findAll(); }

    public List<RentalItemDTO> getRentalItems() {
        return rentalItemRepository.findAll().stream()
                .filter(rentalItem -> rentalItem.getStatus() == ItemStatus.ACTIVE)
                .map(rentalItem -> new RentalItemDTO(
                        rentalItem.getId(),
                        rentalItem.getName(),
                        rentalItem.getDescription(),
                        rentalItem.getPrice(),
                        rentalItem.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    public RentalItem getRentalItemById(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        return rentalItemRepository.findById(rentalItemId).orElse(null);
    }

    public Map<String, Object> addNewRentalItem(RentalItem rentalItem) {
        Optional<RentalItem> rentalItemOptional = rentalItemRepository.findRentalItemByName(rentalItem.getName());
        if (rentalItemOptional.isPresent()) {
            throw new IllegalStateException("Przedmiot o nazwie: " + rentalItem.getName() + " już istnieje");
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
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }

        String message;
        // Sprawdzenie, czy przedmiot figuruje w liście wypożyczeń
        if(rentRepository.existsByItems_RentalItem_Id(rentalItemId)){
            message = "Przedmiot o id " + rentalItemId + " jest powiązany z wypożyczeniem, przedmiot ustawiono jako nieaktywny";
            getRentalItemById(rentalItemId).setStatus(ItemStatus.INACTIVE);
            rentalItemRepository.save(getRentalItemById(rentalItemId));
        } else {
            message = "Przedmiot o id " + rentalItemId + " został usunięty";
            rentalItemRepository.deleteById(rentalItemId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
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
                rentalItem.getQuantity() == null &&
                rentalItem.getDescription() == null &&
                rentalItem.getStatus() == null) {
            throw new IllegalStateException("Należy podać jeden z parametrów");
        }
        if(!rentalItemRepository.existsById(rentalItemId)) {
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }

        RentalItem rentalItemToUpdate = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje"));

        if(rentalItem.getName() != null) {
            if(rentalItemToUpdate.getName().isEmpty()) {
                throw new IllegalStateException("Nazwa nie może być pusta");
            }
            rentalItemToUpdate.setName(rentalItem.getName());
        }

        if(rentalItem.getPrice() != null) {
            if(rentalItemToUpdate.getPrice() == null) {
                throw new IllegalStateException("Cena nie może być pusta");
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

        if(rentalItem.getStatus() != null) {
            if(rentalItemToUpdate.getStatus() == null) {
                throw new IllegalStateException("Status nie może być pusty");
            }
            rentalItemToUpdate.setStatus(rentalItem.getStatus());
        }

        rentalItemRepository.save(rentalItemToUpdate);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> rentRentalItems(List<RentRequest> rentRequests,
                                               Timestamp rentDate,
                                               Timestamp returnDate) {
        Map<String, Object> response = new HashMap<>();
        float totalRentPrice = 0;
        List<RentItem> rentItems = new ArrayList<>();
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());

        // Validate the rental date
        if (rentDate.before(currentTimestamp)) {
            throw new IllegalStateException("Data wypożyczenia nie może być wcześniejsza niż obecna data");
        } else if (returnDate.before(rentDate)) {
            throw new IllegalStateException("Data zwrotu nie może być wcześniejsza niż data wypożyczenia");
        }

        // Calculate the number of rental days
        long milliseconds = returnDate.getTime() - rentDate.getTime();
        int rentalDays = (int) (milliseconds / (1000 * 60 * 60 * 24));

        for (RentRequest request : rentRequests) {
            RentalItem rentalItem = rentalItemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + request.getItemId() + " nie istnieje w wypożyczlni"));

            // Check if the item is active
            if (rentalItem.getStatus() != ItemStatus.ACTIVE) {
                throw new IllegalStateException("Przedmiot o id " + request.getItemId() + " jest nieaktywny");
            }

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
            
            float itemTotalPrice = rentalItem.getPrice() * request.getQuantity() * rentalDays;
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
        rent.setRentDate(rentDate);
        rent.setReturnDate(returnDate);
        rent.setTotalPrice(totalRentPrice);
        rent.setItems(rentItems);

        if (rent.getRentDate().equals(currentTimestamp)) {
            rent.setStatus(RentStatus.RENTED);
        } else {
            rent.setStatus(RentStatus.RESERVED);
        }

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

    @Transactional
    public Map<String, Object> returnRentalItems(Long rentId) {
        Map<String, Object> response = new HashMap<>();

        Rent rent = rentRepository.findById(rentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent o id " + rentId + " nie istnieje"));

        if (rent.getStatus() == RentStatus.RETURNED) {
            throw new IllegalStateException("Wypożyczenie o id " + rentId + " zostało już zwrócone");
        }

        for (RentItem rentItem : rent.getItems()) {
            RentalItem rentalItem = rentItem.getRentalItem();
            rentalItem.setQuantity(rentalItem.getQuantity() + rentItem.getQuantity());
            rentalItemRepository.save(rentalItem);
        }

        rent.setStatus(RentStatus.RETURNED);
        rentRepository.save(rent);

        response.put("message", "Przedmioty zostały zwrócone");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<RentRecordDTO> getRentHistory() {
        Long userId = getCurrentUser().getId();
        List<Rent> rents = rentRepository.findByUserId(userId);
        List<RentRecordDTO> rentRecords = new ArrayList<>();

        for (Rent rent : rents) {
            updateRentStatus(rent);
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
                    rent.getTotalPrice(),
                    rent.getStatus()
            ));
        }

        return rentRecords;
    }

    public List<RentRecordDTO> getRentHistoryAll() {
        List<Rent> rents = rentRepository.findAll();
        List<RentRecordDTO> rentRecords = new ArrayList<>();

        for (Rent rent : rents) {
            updateRentStatus(rent);
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
                    rent.getTotalPrice(),
                    rent.getStatus()
            ));
        }

        return rentRecords;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void updateRentStatus(Rent rent) {
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        if (rent.getStatus() == RentStatus.RESERVED && rent.getRentDate().before(currentTimestamp)) {
            rent.setStatus(RentStatus.RENTED);
            rentRepository.save(rent);
        }
    }
}

