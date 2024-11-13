package pwr.isa.klama.rentalItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class RentalItem {


    @Id
    @SequenceGenerator(
            name = "rentalItem_sequence",
            sequenceName = "rentalItem_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rentalItem_sequence"
    )
    private Long id;
    private String name;
    private String description;
    private Float price;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    public RentalItem() {
    }

    public RentalItem(Long id,
                      String name,
                      String description,
                      Float price,
                      Integer quantity,
                      ItemStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    public RentalItem(String name,
                      String description,
                      Float price,
                      Integer quantity,
                      ItemStatus status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    @Override
    public String toString() {
        return "RentalItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
