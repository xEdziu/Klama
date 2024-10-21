package pwr.isa.klama.rentalItem;

import jakarta.persistence.*;

@Entity
@Table
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

    public RentalItem() {
    }

    public RentalItem(Long id,
                      String name,
                      String description,
                      Float price,
                      Integer quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public RentalItem(String name,
                      String description,
                      Float price,
                      Integer quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
