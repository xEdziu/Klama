package pwr.isa.klama.user;

import lombok.Getter;

@Getter
public class UserDTO {
    private final Long id;
    private final String firstName;
    private final String surname;
    private final String username;
    private final String email;

    public UserDTO(Long id, String firstName, String surname, String username, String email) {
        this.id = id;
        this.firstName = firstName;
        this.username = username;
        this.email = email;
        this.surname = surname;
    }
}
