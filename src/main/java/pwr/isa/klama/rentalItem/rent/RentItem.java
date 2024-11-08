package pwr.isa.klama.rentalItem.rent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pwr.isa.klama.rentalItem.RentalItem;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class RentItem {
    @Id
    @SequenceGenerator(
            name = "rentItem_sequence",
            sequenceName = "rentItem_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rentItem_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rent_id", nullable = false)
    private Rent rent;

    @ManyToOne
    @JoinColumn(name = "rental_item_id", nullable = false)
    private RentalItem rentalItem;

    private int quantity;
    private float price;
    private float totalPrice;
}
