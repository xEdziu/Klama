package pwr.isa.klama.pass.userPass;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class UserPassHistoryDTO {
    private Long passId;
    private String passName;
    private Float price;
    private Timestamp buyDate;
    private Timestamp expirationDate;


    public UserPassHistoryDTO(Long passId,
                              String passName,
                              Timestamp buyDate,
                              Timestamp expirationDate,
                              Float price) {
        this.passId = passId;
        this.passName = passName;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
        this.price = price;
    }
}
