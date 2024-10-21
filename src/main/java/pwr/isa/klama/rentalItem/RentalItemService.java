package pwr.isa.klama.rentalItem;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
            throw new IllegalArgumentException("Rental Item with id " + rentalItemId + " does not exist");
        }
        return rentalItemRepository.findById(rentalItemId).orElse(null);
    }

    public void addNewRentalItem(RentalItem rentalItem) {
        rentalItemRepository.save(rentalItem);

    }

    public void deleteRentalItem(Long rentalItemId) {
        boolean exists = rentalItemRepository.existsById(rentalItemId);
        if( !exists ) {
            throw new IllegalArgumentException("Rental Item with id " + rentalItemId + " does not exist");
        }
        rentalItemRepository.deleteById(rentalItemId);
    }

    @Transactional
    public void updateRentalItem(Long rentalItemId,
                                 String name,
                                 String description,
                                 Float price,
                                 Integer quantity) {
        RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rental Item with id " + rentalItemId + " does not exist"));

        if (name != null && name.length() > 0
                && !Objects.equals(rentalItem.getName(), name)) {
            rentalItem.setName(name);
        }

        if (description != null && description.length() > 0
                && !Objects.equals(rentalItem.getDescription(), description)) {
            rentalItem.setDescription(description);
        }

        if (price != null && price > 0
                && !Objects.equals(rentalItem.getPrice(), price)) {
            rentalItem.setPrice(price);
        }

        if (quantity != null && quantity > 0 && !Objects.equals(rentalItem.getQuantity(), quantity)) {
            rentalItem.setQuantity(quantity);
        }
    }


}

