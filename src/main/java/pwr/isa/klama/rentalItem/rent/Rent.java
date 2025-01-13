package pwr.isa.klama.rentalItem.rent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pwr.isa.klama.rentalItem.RentalItem;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Rent {
    @Id
    @SequenceGenerator(
            name = "rent_sequence",
            sequenceName = "rent_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rent_sequence"
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Timestamp rentDate;
    private Timestamp returnDate;
    private float totalPrice;

    @OneToMany(mappedBy = "rent", cascade = CascadeType.ALL)
    private List<RentItem> items;

    @Enumerated(EnumType.STRING)
    private RentStatus status;

    public Timestamp getDate() {
        return rentDate;
    }
}
