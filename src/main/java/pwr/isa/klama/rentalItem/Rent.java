package pwr.isa.klama.rentalItem;

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
public class Rent {
    @Id
    @SequenceGenerator(
            name = "rent_sequence",
            sequenceName = "rent_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rent_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "rental_item_id", nullable = false)
    private RentalItem rentalItem;
    private int quantity;
    private Timestamp rentDate;
    private Timestamp returnDate;
    private float totalPrice;
}
