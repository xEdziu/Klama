package pwr.isa.klama.auth.registration;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.EmailValidator;
import pwr.isa.klama.auth.registration.token.ConfirmationToken;
import pwr.isa.klama.auth.registration.token.ConfirmationTokenService;
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
    private final ConfirmationTokenService confirmationTokenService;

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

    @Transactional
    public String confirmToken(String token) {

        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token);

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        Timestamp expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.before(new Timestamp(new Date().getTime()))) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(
                confirmationToken.getUser().getEmail());
        return "confirmed";
    }
}
