package pwr.isa.klama.auth.registration;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.EmailValidator;
import pwr.isa.klama.user.User;
import pwr.isa.klama.user.UserRole;
import pwr.isa.klama.user.UserService;

import java.util.Date;
import java.sql.Timestamp;

@Service
@AllArgsConstructor
public class RegisterService {

    private final UserService userService;
    private final EmailValidator emailValidator;

    public String register(RegisterRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail)
            throw new IllegalStateException("Email not valid");
        return userService.signUpUser(
                new User(
                        request.getUsername(),
                        request.getFirstName(),
                        request.getSurname(),
                        request.getEmail(),
                        request.getPassword(),
                        UserRole.USER,
                        new Timestamp(new Date().getTime()),
                        null
                )
        );
    }
}
