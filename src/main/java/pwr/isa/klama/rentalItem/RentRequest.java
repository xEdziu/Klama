package pwr.isa.klama.rentalItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentRequest {
    private Long itemId;
    private int quantity;
}
