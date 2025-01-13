package pwr.isa.klama.pass.userPass;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pwr.isa.klama.pass.Pass;
import pwr.isa.klama.pass.PassStatus;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;

@Table
@Entity
@Getter
@Setter
public class UserPass {
    @Id
    @SequenceGenerator(
            name = "userPass_sequence",
            sequenceName = "userPass_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "userPass_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "pass_id", nullable = false)
    private Pass pass;

    @Enumerated(EnumType.STRING)
    private UserPassStatus status;

    private Timestamp buyDate;
    private Timestamp expirationDate;

    public UserPass() {
    }

    public UserPass(Long id,
                    User user,
                    Pass pass,
                    UserPassStatus status,
                    Timestamp buyDate,
                    Timestamp expirationDate) {
        this.id = id;
        this.user = user;
        this.pass = pass;
        this.status = status;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
    }

    public UserPass(User user,
                    Pass pass,
                    UserPassStatus status,
                    Timestamp buyDate,
                    Timestamp expirationDate) {
        this.user = user;
        this.pass = pass;
        this.status = status;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "UserPass{" +
                "id=" + id +
                ", user=" + user +
                ", pass=" + pass +
                ", status=" + status +
                ", buyDate=" + buyDate +
                ", expirationDate=" + expirationDate +
                '}';
    }

    public Timestamp getDate() {
        return buyDate;
    }

    public Float getPrice() {
        return pass.getPrice();
    }
}
