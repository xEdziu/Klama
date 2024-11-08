package pwr.isa.klama.rentalItem.rent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentRequest {
    private Long itemId;
    private int quantity;
}
