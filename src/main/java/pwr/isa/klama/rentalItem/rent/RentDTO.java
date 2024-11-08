package pwr.isa.klama.rentalItem.rent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RentDTO {
    private String itemName;
    private Integer quantity;
    private Float price;
    private Float totalPrice;
}
