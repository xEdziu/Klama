package pwr.isa.klama.auth.registration;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.EmailValidator;
import pwr.isa.klama.auth.registration.token.ConfirmationToken;
import pwr.isa.klama.auth.registration.token.ConfirmationTokenService;
import pwr.isa.klama.user.User;
import pwr.isa.klama.email.EmailSender;
import pwr.isa.klama.user.UserRole;
import pwr.isa.klama.user.UserService;

import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class RegisterService {

    private final UserService userService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    public Map<String, Object> register(RegisterRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if (!isValidEmail)
            throw new IllegalStateException("Email nie jest poprawny");

        String token = userService.signUpUser(
                new User(
                        request.getUsername(),
                        request.getFirstName(),
                        request.getSurname(),
                        request.getEmail(),
                        request.getPassword(),
                        UserRole.ROLE_USER,
                        new Timestamp(new Date().getTime())
                )
        );

        String link = "http://localhost:8080/api/v1/register/confirm?token=" + token;
        emailSender.send(
                request.getEmail(),
                buildConfirmationEmail(request.getFirstName(), link),
                "Aktywacja konta | KLAMA"
        );
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email aktywacyjny został wysłany na Twoją skrzynkę pocztową");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("redirect", "http://localhost:8080/login");
        response.put("token", token);
        return response;
    }

    private String buildConfirmationEmail(String firstName, String link) {
        return "Witaj " + firstName + ",<br/>" +
                "Kliknij <a href=\"" + link + "\">tutaj</a>, aby aktywować konto";
    }

    @Transactional
    public Map<String, Object> confirmToken(String token) {

        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token);

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Adres email jest już potwierdzony");
        }

        Timestamp expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.before(new Timestamp(new Date().getTime()))) {
            throw new IllegalStateException("Token wygasł. Przejdź do strony rejestracji i podaj te " +
                    "same dane, aby otrzymać nowy link aktywacyjny");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(
                confirmationToken.getUser().getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Konto zostało aktywowane");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("redirect", "http://localhost:8080/login");
        return response;
    }
}
