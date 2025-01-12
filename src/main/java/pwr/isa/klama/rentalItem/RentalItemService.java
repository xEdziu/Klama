package pwr.isa.klama.rentalItem;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.rentalItem.rent.*;
import pwr.isa.klama.security.logging.ApiLogger;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
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

    public List<RentalItem> getRentalItemsForAdmin() {
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/all", "Getting all rental items for admin");
        return rentalItemRepository.findAll();
    }

    public List<RentalItemDTO> getRentalItems() {
        ApiLogger.logInfo("/api/v1/rentalItem/all", "Getting all rental items");
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
        ApiLogger.logInfo("/api/v1/rentalItem/" + rentalItemId, "Getting rental item: " + rentalItemId);
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            ApiLogger.logSevere("/api/v1/rentalItem/" + rentalItemId, "Rental item " + rentalItemId + " not found");
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        return rentalItemRepository.findById(rentalItemId).orElse(null);
    }

    public Map<String, Object> addNewRentalItem(RentalItem rentalItem) {
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/add", "Adding new rental item: " + rentalItem.getName());

        Optional<RentalItem> rentalItemOptional = rentalItemRepository.findRentalItemByName(rentalItem.getName());
        if (rentalItemOptional.isPresent()) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/add", "Rental item with name " + rentalItem.getName() + " already exists");
            throw new IllegalStateException("Przedmiot o nazwie: " + rentalItem.getName() + " już istnieje");
        }
        if (rentalItem.getPrice() <= 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/add", "Price cannot be less than 0");
            throw new IllegalStateException("Cena nie może być mniejsza niż 0");
        }
        if (rentalItem.getQuantity() < 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/add", "Quantity cannot be less than 0");
            throw new IllegalStateException("Ilość nie może być mniejsza niż 0");
        }
        rentalItemRepository.save(rentalItem);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot został dodany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/add", "Rental item " + rentalItem.getName() + " added successfully");
        return response;
    }

    public Map<String, Object> deleteRentalItem(Long rentalItemId) {
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/delete/" + rentalItemId, "Deleting rental item: " + rentalItemId);
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if (!exists) {
            ApiLogger.logSevere("/api/v1/authorized/admin/rentalItem/delete/" + rentalItemId, "Rental item " + rentalItemId + " not found");
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }

        String message;
        // Sprawdzenie, czy przedmiot figuruje w liście wypożyczeń
        if(rentRepository.existsByItems_RentalItem_Id(rentalItemId)){
            message = "Przedmiot o id " + rentalItemId + " jest powiązany z wypożyczeniem, przedmiot ustawiono jako nieaktywny";
            getRentalItemById(rentalItemId).setStatus(ItemStatus.INACTIVE);
            rentalItemRepository.save(getRentalItemById(rentalItemId));
            ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/delete/" + rentalItemId, "Deleting impossible, Hiding rental item: " + rentalItemId);
        } else {
            message = "Przedmiot o id " + rentalItemId + " został usunięty";
            rentalItemRepository.deleteById(rentalItemId);
            ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/delete/" + rentalItemId, "Deleting rental item: " + rentalItemId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> updateRentalItem(Long rentalItemId,
                                                RentalItem rentalItem) {
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Updating rental item: " + rentalItemId);

        if (rentalItem.getQuantity() != null && rentalItem.getQuantity() < 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Quantity cannot be less than 0");
            throw new IllegalStateException("Ilość nie może być mniejsza niż 0");
        }

        if (rentalItem.getPrice() != null && rentalItem.getPrice() < 0) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Price cannot be less than 0");
            throw new IllegalStateException("Cena nie może być mniejsza niż 0");
        }

        if(rentalItem.getName() == null &&
                rentalItem.getId() == null &&
                rentalItem.getPrice() == null &&
                rentalItem.getQuantity() == null &&
                rentalItem.getDescription() == null &&
                rentalItem.getStatus() == null) {
            ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "No parameters provided for update");
            throw new IllegalStateException("Należy podać jeden z parametrów");
        }
        if (!rentalItemRepository.existsById(rentalItemId)) {
            ApiLogger.logSevere("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item " + rentalItemId + " not found");
            throw new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }

        RentalItem rentalItemToUpdate = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Przedmiot o id " + rentalItemId + " nie istnieje"));

        if(rentalItem.getName() != null) {
            if(rentalItemToUpdate.getName().isEmpty()) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item name cannot be empty");
                throw new IllegalStateException("Nazwa nie może być pusta");
            }
            rentalItemToUpdate.setName(rentalItem.getName());
        }

        if(rentalItem.getPrice() != null) {
            if(rentalItemToUpdate.getPrice() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item price cannot be empty");
                throw new IllegalStateException("Cena nie może być pusta");
            }
            if (rentalItem.getPrice() <= 0) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Price cannot be less than 0");
                throw new IllegalStateException("Cena nie może być mniejsza lub równa 0");
            }
            rentalItemToUpdate.setPrice(rentalItem.getPrice());
        }

        if(rentalItem.getQuantity() != null) {
            if(rentalItemToUpdate.getQuantity() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item quantity cannot be empty");
                throw new IllegalStateException("Ilość nie może być pusta");
            }
            if (rentalItem.getQuantity() < 0) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Quantity cannot be less than 0");
                throw new IllegalStateException("Ilość nie może być mniejsza niż 0");
            }
            rentalItemToUpdate.setQuantity(rentalItem.getQuantity());
        }

        if(rentalItem.getDescription() != null) {
            if(rentalItemToUpdate.getDescription().isEmpty()) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item description cannot be empty");
                throw new IllegalStateException("Opis nie może być pusty");
            }
            rentalItemToUpdate.setDescription(rentalItem.getDescription());
        }

        if(rentalItem.getStatus() != null) {
            if(rentalItemToUpdate.getStatus() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item status cannot be empty");
                throw new IllegalStateException("Status nie może być pusty");
            }
            rentalItemToUpdate.setStatus(rentalItem.getStatus());
        }

        rentalItemRepository.save(rentalItemToUpdate);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Przedmiot został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/update/" + rentalItemId, "Rental item " + rentalItemId + " updated successfully");
        return response;
    }

    @Transactional
    public Map<String, Object> rentRentalItems(List<RentRequest> rentRequests,
                                               Timestamp rentDate,
                                               Timestamp returnDate) {
        Long userId = getCurrentUser().getId();
        ApiLogger.logInfo("/api/v1/authorized/rentalItem/rent", "Renting items for user: " + userId);
        Map<String, Object> response = new HashMap<>();
        float totalRentPrice = 0;
        List<RentItem> rentItems = new ArrayList<>();
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());

        // Validate the rental date
        if (rentDate.before(currentTimestamp)) {
            ApiLogger.logWarning("/api/v1/authorized/rentalItem/rent", "Rental date cannot be before current date");
            throw new IllegalStateException("Data wypożyczenia nie może być wcześniejsza niż obecna data");
        } else if (returnDate.before(rentDate)) {
            ApiLogger.logWarning("/api/v1/authorized/rentalItem/rent", "Return date cannot be before rental date");
            throw new IllegalStateException("Data zwrotu nie może być wcześniejsza niż data wypożyczenia");
        }

        // Calculate the number of rental days
        long milliseconds = returnDate.getTime() - rentDate.getTime();
        int rentalDays = (int) (milliseconds / (1000 * 60 * 60 * 24));

        for (RentRequest request : rentRequests) {
            RentalItem rentalItem = rentalItemRepository.findById(request.getItemId())
                    .orElseThrow(() -> {
                        ApiLogger.logSevere("/api/v1/authorized/rentalItem/rent", "Rental item " + request.getItemId() + " not found");
                        return new ResourceNotFoundException("Przedmiot o id " + request.getItemId() + " nie istnieje w wypożyczlni");
                    });

            if (rentalItem.getStatus() != ItemStatus.ACTIVE) {
                ApiLogger.logWarning("/api/v1/authorized/rentalItem/rent", "Rental item " + request.getItemId() + " is inactive");
                throw new IllegalStateException("Przedmiot o id " + request.getItemId() + " jest nieaktywny");
            }

            if (request.getQuantity() <= 0) {
                ApiLogger.logWarning("/api/v1/authorized/rentalItem/rent", "Invalid quantity for rental item " + request.getItemId());
                throw new IllegalStateException("Niepoprawna ilość przedmiotów");
            }

            if (rentalItem.getQuantity() < request.getQuantity()) {
                ApiLogger.logWarning("/api/v1/authorized/rentalItem/rent", "Insufficient quantity for rental item " + request.getItemId());
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

        LocalDate rentalDate = rent.getRentDate().toLocalDateTime().toLocalDate();
        LocalDate currentDate = currentTimestamp.toLocalDateTime().toLocalDate();

        if (rentalDate.equals(currentDate)) {
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

        ApiLogger.logInfo("/api/v1/authorized/rentalItem/rent", "Items rented successfully for user: " + userId);
        return response;
    }

    @Transactional
    public Map<String, Object> returnRentalItems(Long rentId) {
        ApiLogger.logInfo("/api/v1/authorized/rentalItem/return/" + rentId, "Returning rental items for rent: " + rentId);
        Map<String, Object> response = new HashMap<>();

        Rent rent = rentRepository.findById(rentId)
                .orElseThrow(() -> {
                    ApiLogger.logSevere("/api/v1/authorized/rentalItem/return/" + rentId, "Rent " + rentId + " not found");
                    return new ResourceNotFoundException("Rent o id " + rentId + " nie istnieje");
                });

        if (rent.getStatus() == RentStatus.RETURNED) {
            ApiLogger.logWarning("/api/v1/authorized/rentalItem/return/" + rentId, "Rent " + rentId + " already returned");
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

        ApiLogger.logInfo("/api/v1/authorized/rentalItem/return/" + rentId, "Rental items returned successfully for rent: " + rentId);
        return response;
    }

    public List<RentRecordDTO> getRentHistory() {
        Long userId = getCurrentUser().getId();
        ApiLogger.logInfo("/api/v1/authorized/rentalItem/rentHistory", "Getting rent history for user: " + userId);
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
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/rentHistory/all", "Getting all users rent history for Admin");
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

    public List<RentRecordDTO> getRentHistoryByUserId(Long userId) {
        ApiLogger.logInfo("/api/v1/authorized/admin/rentalItem/history/" + userId, "Getting rent history for user: " + userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Użytkownik o id " + userId + " nie istnieje");
        }

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

