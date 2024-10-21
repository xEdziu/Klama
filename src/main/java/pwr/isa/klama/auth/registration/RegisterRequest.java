package pwr.isa.klama.auth.registration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegisterRequest {
    private final String username;
    private final String firstName;
    private final String surname;
    private final String email;
    private final String password;
}