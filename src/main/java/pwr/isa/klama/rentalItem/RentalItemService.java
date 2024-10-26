package pwr.isa.klama.rentalItem;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class RentalItemService {

    private final RentalItemRepository rentalItemRepository;

    @Autowired
    public RentalItemService(RentalItemRepository rentalItemRepository) {
        this.rentalItemRepository = rentalItemRepository;
    }

    public List<RentalItem> getRentalItem() {
        return rentalItemRepository.findAll();
    }

    public RentalItem getRentalItemById(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new IllegalArgumentException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        return rentalItemRepository.findById(rentalItemId).orElse(null);
    }

    public void addNewRentalItem(RentalItem rentalItem) {
        rentalItemRepository.save(rentalItem);

    }

    public void deleteRentalItem(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new IllegalArgumentException("Przedmiot o id " + rentalItemId + " nie istnieje");
        }
        rentalItemRepository.deleteById(rentalItemId);
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
}

