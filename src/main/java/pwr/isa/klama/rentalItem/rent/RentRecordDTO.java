package pwr.isa.klama.rentalItem.rent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RentRecordDTO {
    private Long rentId;
    private Long userId;
    private Date rentDate;
    private Date returnDate;
    private List<RentDTO> items;
    private Float totalPrice;
    private RentStatus status;
}
