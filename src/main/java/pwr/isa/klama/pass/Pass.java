package pwr.isa.klama.pass;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pwr.isa.klama.rentalItem.ItemStatus;

@Entity
@Table
@Getter
@Setter
public class Pass {

    @Id
    @SequenceGenerator(
            name = "pass_sequence",
            sequenceName = "pass_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pass_sequence"
    )
    private Long id;
    private String name;
    private String description;
    private Float price;
    private Integer days;
    @Enumerated(EnumType.STRING)
    private PassStatus status;

    public Pass() {
    }

    public Pass(Long id,
                String name,
                String description,
                Float price,
                Integer days,
                PassStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.days = days;
        this.status = status;
    }

    public Pass(String name,
                String description,
                Float price,
                Integer days,
                PassStatus status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.days = days;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Pass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", days=" + days +
                ", status=" + status +
                '}';
    }
}
