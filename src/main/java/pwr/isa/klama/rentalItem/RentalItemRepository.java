package pwr.isa.klama.rentalItem;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalItemRepository
        extends JpaRepository<RentalItem, Long> {
}
