package pwr.isa.klama.shop;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequest {
    private Long itemId;
    private int quantity;
}