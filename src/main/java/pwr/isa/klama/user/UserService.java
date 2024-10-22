package pwr.isa.klama.user;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.registration.token.ConfirmationToken;
import pwr.isa.klama.auth.registration.token.ConfirmationTokenService;
import pwr.isa.klama.email.EmailSender;
import pwr.isa.klama.exceptions.AccountNotActivatedException;
import pwr.isa.klama.exceptions.ResourceNotFoundException;

import java.sql.Timestamp;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG_USERNAME = "Nie znaleziono użytkownika o nicku %s";
    private final static String USER_NOT_FOUND_MSG_EMAIL = "Nie znaleziono użytkownika o emailu %s";
    private final static String USER_NOT_FOUND_MSG_USERNAME_OR_EMAIL = "Nie znaleziono użytkownika o nicku %s lub emailu %s";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    public void enableUser(String email) {
        userRepository.enableUser(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG_USERNAME, username)));
    }

    public UserDetails loadUserById(Long id) throws ResourceNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG_USERNAME, id)));
    }

    public UserDetails loadUserByEmail(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG_EMAIL, email)));
    }

    public UserDetails loadUserByUsernameOrEmail(String username, String email) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG_USERNAME_OR_EMAIL, username, email)));
    }

    @Transactional
    public String signUpUser(User user) {
        Optional<User> existingUserOpt = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail());

        String token = UUID.randomUUID().toString();

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (!existingUser.getEnabled()) {
                if (!existingUser.getEmail().equals(user.getEmail())) {
                    throw new IllegalStateException("Nazwa użytkownika jest już zajęta");
                } else {
                    ConfirmationToken confirmationToken = new ConfirmationToken(
                            token,
                            new Timestamp(new Date().getTime()),
                            // 15 minutes
                            new Timestamp(new Date().getTime() + 1000 * 60 * 15),
                            existingUser
                    );
                    confirmationTokenService.saveConfirmationToken(confirmationToken);

                    // Send email with the new token
                    String link = "http://localhost:8080/api/v1/register/confirm?token=" + token;
                    emailSender.send(
                            existingUser.getEmail(),
                            buildConfirmationEmail(existingUser.getFirstName(), link),
                            "Aktywacja konta | KLAMA"
                    );
                    throw new AccountNotActivatedException("Konto nie zostało jeszcze aktywowane, sprawdź email w celu aktywacji");
                }
            } else {
                throw new IllegalStateException("Nazwa użytkownika lub email są już zajęte");
            }
        }

        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                new Timestamp(new Date().getTime()),
                // 15 minutes
                new Timestamp(new Date().getTime() + 1000 * 60 * 15),
                user
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    private String buildConfirmationEmail(String firstName, String link) {
        return "Witaj " + firstName + ",<br/>" +
                "Kliknij <a href=\"" + link + "\">tutaj</a>, aby aktywować konto";
    }

    public UserDTO getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalStateException("Użytkownik o nicku " + username + " nie istnieje");
        }
        return new UserDTO(user.get().getId(), user.get().getFirstName(), user.get().getSurname(), user.get().getUsername(), user.get().getEmail());
    }

    public UserDTO getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje");
        }
        if (user.get().getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Nie można wyświetlić danych - ADM_ERR");
        }
        return new UserDTO(user.get().getId(), user.get().getFirstName(), user.get().getSurname(), user.get().getUsername(), user.get().getEmail());
    }

    @Transactional
    public Map<String, Object> updateUser(Long userId, User user) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje");
        }

        if (userOpt.get().getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Nie można edytować danych - ADM_ERR");
        }

        User existingUser = userOpt.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setSurname(user.getSurname());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(existingUser);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deleteUser(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje, nie można go usunąć");
        }

        if (userRepository.findById(userId).isPresent() && userRepository.findById(userId).get().getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Nie można usunąć użytkownika - ADM_ERR");
        }

        userRepository.deleteById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Map<String, Object> updateUserAdmin(Long userId, User user) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje");
        }

        User existingUser = userOpt.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setSurname(user.getSurname());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(existingUser);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deleteUserAdmin(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje, nie można go usunąć");
        }

        userRepository.deleteById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }
}
