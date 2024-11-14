package pwr.isa.klama.pass;

import lombok.Getter;

@Getter
public class PassDTO {
    private final Long id;
    private final String name;
    private final String description;
    private final Float price;
    private final Integer days;

    public PassDTO(Long id,
                   String name,
                   String description,
                   Float price,
                   Integer days) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.days = days;
    }
}
