package pwr.isa.klama.pass.userPass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPassDTO {
    private Long id;
    private Long userId;
    private Long passId;
    private String status;
    private String buyDate;
    private String expirationDate;

    public UserPassDTO(Long id,
                       Long userId,
                       Long passId,
                       String status,
                       String buyDate,
                       String expirationDate) {
        this.id = id;
        this.userId = userId;
        this.passId = passId;
        this.status = status;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
    }
}
