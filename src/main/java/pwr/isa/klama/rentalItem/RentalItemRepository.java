package pwr.isa.klama.rentalItem;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {
    Optional<RentalItem> findRentalItemByName(String rentalItemName);
}
