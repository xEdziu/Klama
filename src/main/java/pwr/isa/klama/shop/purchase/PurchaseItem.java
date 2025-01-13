package pwr.isa.klama.shop.purchase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pwr.isa.klama.shop.ShopItems;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PurchaseItem {
    @Id
    @SequenceGenerator(
            name = "purchaseItem_sequence",
            sequenceName = "purchaseItem_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "purchaseItem_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "shop_item_id", nullable = false)
    private ShopItems shopItem;

    private int quantity;
    private float price;

    @Transient
    private float totalPrice;

    public float getTotalPrice() {
        return quantity * price;
    }

    public void setTotalPrice() {
        totalPrice = quantity * price;
    }

    public PurchaseItem(Purchase purchase, ShopItems shopItem, int quantity, float price) {
        this.purchase = purchase;
        this.shopItem = shopItem;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = getTotalPrice();
    }
}