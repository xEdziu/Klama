package pwr.isa.klama.shop.purchase;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO {
    private String itemName;
    private Integer quantity;
    private Float price;
    private Float totalPrice;
}
