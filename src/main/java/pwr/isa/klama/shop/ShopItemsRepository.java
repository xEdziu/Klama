package pwr.isa.klama.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ShopItemsRepository
        extends JpaRepository<ShopItems, Long> {
    Optional<Object> findByName(String name);
    Optional<Object> findByDescription(String description);
    List<ShopItems> findByActive(boolean active);
}
