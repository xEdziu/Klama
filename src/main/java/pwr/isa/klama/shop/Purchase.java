package pwr.isa.klama.shop;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Purchase {
    @Id
    @SequenceGenerator(
            name = "purchase_sequence",
            sequenceName = "purchase_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "purchase_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "shop_item_id", nullable = false)
    private ShopItems shopItem;
    private int quantity;
    private Timestamp purchaseDate;
    private float totalPrice;
}