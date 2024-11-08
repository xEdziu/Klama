package pwr.isa.klama.shop.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByItems_ShopItem_Id(Long shopItemId);

    List<Purchase> findByUserId(Long userId);
}