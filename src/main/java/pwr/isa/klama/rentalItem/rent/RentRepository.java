package pwr.isa.klama.rentalItem.rent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface RentRepository extends JpaRepository<Rent, Long> {
    boolean existsByItems_RentalItem_Id(Long rentItemId);

    List<Rent> findByUserId(Long userId);

    List<Rent> findAllByRentDateBetween(Timestamp timestamp, Timestamp timestamp1);
}
