package pwr.isa.klama.auth.registration.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public void setConfirmedAt(String token) {
        confirmationTokenRepository.updateConfirmedAt(
                token, new Timestamp(new Date().getTime()));
    }

    public ConfirmationToken getToken(String token) {
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));
    }
}
