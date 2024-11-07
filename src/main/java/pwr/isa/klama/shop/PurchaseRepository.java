package pwr.isa.klama.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByShopItemId(Long shopItemId);
    List<Purchase> findByUserId(Long userId);
}