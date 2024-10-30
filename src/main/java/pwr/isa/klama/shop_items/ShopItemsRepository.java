package pwr.isa.klama.shop_items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopItemsRepository
        extends JpaRepository<ShopItems, Integer> {
}
