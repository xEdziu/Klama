package pwr.isa.klama.shop.purchase;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    private Timestamp purchaseDate;
    private float totalPrice;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL)
    private List<PurchaseItem> items;

    public Purchase(User user, Timestamp purchaseDate, float totalPrice, List<PurchaseItem> purchaseItems) {
        this.user = user;
        this.purchaseDate = purchaseDate;
        this.totalPrice = totalPrice;
        this.items = purchaseItems;
    }

    public Timestamp getDate() {
        return purchaseDate;
    }
}