package pwr.isa.klama.shop;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseRecordDTO {
    private Long purchaseId;
    private List<PurchaseDTO> items;
    private Float totalPrice;
}
