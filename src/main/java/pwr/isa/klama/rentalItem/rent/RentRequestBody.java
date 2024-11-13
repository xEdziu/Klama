package pwr.isa.klama.rentalItem.rent;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class RentRequestBody {
    private List<RentRequest> rentRequests;
    private Timestamp rentDate;
    private Timestamp returnDate;
}
