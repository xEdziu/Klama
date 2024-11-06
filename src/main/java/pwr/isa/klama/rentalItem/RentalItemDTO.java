package pwr.isa.klama.rentalItem;

import lombok.Getter;

@Getter
public class RentalItemDTO {
    private final Long id;
    private final String name;
    private final String description;
    private final Float price;
    private final Integer quantity;

    public RentalItemDTO(Long id, String name, String description, Float price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
}
