package pwr.isa.klama.user;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.EmailValidator;
import pwr.isa.klama.auth.PasswordValidator;
import pwr.isa.klama.auth.registration.token.ConfirmationToken;
import pwr.isa.klama.auth.registration.token.ConfirmationTokenService;
import pwr.isa.klama.email.EmailSender;
import pwr.isa.klama.exceptions.AccountNotActivatedException;
import pwr.isa.klama.exceptions.ForbiddenActionException;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.pass.userPass.UserPass;
import pwr.isa.klama.pass.userPass.UserPassRepository;
import pwr.isa.klama.posts.Post;
import pwr.isa.klama.posts.PostRepository;
import pwr.isa.klama.posts.PostService;
import pwr.isa.klama.rentalItem.rent.Rent;
import pwr.isa.klama.rentalItem.rent.RentRepository;
import pwr.isa.klama.security.logging.ApiLogger;
import pwr.isa.klama.shop.purchase.Purchase;
import pwr.isa.klama.shop.purchase.PurchaseRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG_USERNAME = "Nie znaleziono użytkownika o nicku %s";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final PostService postService;
    private final PasswordValidator passwordValidator;
    private final EmailValidator emailValidator;
    private final PostRepository postRepository;
    private final RentRepository rentRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserPassRepository userPassRepository;

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

    @Transactional
    public String signUpUser(User user) {
        ApiLogger.logInfo("as above","Signing up user: " + user.getUsername());
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

        boolean isValidEmail = emailValidator.test(user.getEmail());
        boolean isValidPassword = passwordValidator.test(user.getPassword());

        if (!isValidEmail)
            throw new IllegalStateException("Email nie jest poprawny");

        if (!isValidPassword)
            throw new IllegalStateException("Hasło nie spełnia wymagań bezpieczeństwa");

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

    public UserDTO getUserInfo() {
        User user = (User) getAuthentication().getPrincipal();

        ApiLogger.logInfo("/api/v1/authorized/user","Getting user info: " + user.getId());
        if (!user.getEnabled()) {
            throw new AccountNotActivatedException("Konto nie zostało jeszcze aktywowane, sprawdź email w celu aktywacji");
        }

        return new UserDTO(user.getId(), user.getFirstName(), user.getSurname(), user.getUsername(), user.getEmail());
    }

    public Map<String, Object> updateUser(User user) {
        User existingUser = (User) getAuthentication().getPrincipal();
        ApiLogger.logInfo("/api/v1/authorized/user/update","Updating user: " + existingUser.getId());
        boolean isValidEmail = emailValidator.test(user.getEmail());
        if (!isValidEmail)
            throw new IllegalStateException("Email nie jest poprawny");
        existingUser.setFirstName(user.getFirstName());
        existingUser.setSurname(user.getSurname());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        userRepository.save(existingUser);
        Map<String, Object> response = new HashMap<>();
        if (!user.getPassword().isEmpty()) {
            response.put("warning", "Hasło nie może być zmienione poprzez ten formularz. Użyj formularza zmiany hasła");
            ApiLogger.logWarning("/api/v1/authorized/user/update","Password cannot be changed through this form. Use password change form. User: " + existingUser.getId());
        }
        response.put("message", "Użytkownik o id " + existingUser.getId() + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }


    public Map<String, Object> deleteUser() {
        User user = (User) getAuthentication().getPrincipal();
        Long userId = user.getId();

        ApiLogger.logInfo("/api/v1/authorized/user/delete","Deleting user: " + userId);
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje, nie można go usunąć");
        }

        if (userRepository.findById(userId).isPresent() && userRepository.findById(userId).get().getRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalStateException("Nie można usunąć użytkownika - ADM_ERR");
        }

        User defaultAdmin = userRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono domyślnego administratora"));

        transferUserAssetsToDefaultAdmin(userId, defaultAdmin);

        userRepository.deleteById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public List<User> getAllUsers() {
        ApiLogger.logInfo("/api/v1/authorized/admin/user/all","Getting all users");
        return userRepository.findAll();
    }

    public Map<String, Object> updateUserAdmin(Long userId, User user) {
        Optional<User> userOpt = userRepository.findById(userId);
        ApiLogger.logInfo("/api/v1/authorized/admin/user/update","Updating user: " + userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje");
        }

        boolean isValidEmail = emailValidator.test(user.getEmail());
        boolean isValidPassword = passwordValidator.test(user.getPassword());

        if (!isValidEmail)
            throw new IllegalStateException("Email nie jest poprawny");

        if (!isValidPassword)
            throw new IllegalStateException("Hasło nie spełnia wymagań bezpieczeństwa");

        User existingUser = userOpt.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setSurname(user.getSurname());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        // Allowed to change password here, because of specific conditions, when user forgot his password
        // and asked admin to change it
        existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(existingUser);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> deleteUserAdmin(Long userId) {
        boolean exists = userRepository.existsById(userId);
        ApiLogger.logInfo("/api/v1/authorized/admin/user/delete","Deleting user: " + userId);
        if (!exists) {
            throw new IllegalStateException("Użytkownik o id " + userId + " nie istnieje, nie można go usunąć");
        }
        if (userId==1L) {
            throw new ForbiddenActionException("Nie można usunąć domyślnego administratora");
        }

        User defaultAdmin = userRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono domyślnego administratora"));

        transferUserAssetsToDefaultAdmin(userId, defaultAdmin);

        userRepository.deleteById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Użytkownik o id " + userId + " został usunięty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Map<String, Object> updateUserPassword(User password) {
        User user = (User) getAuthentication().getPrincipal();
        ApiLogger.logInfo("/api/v1/authorized/user/update/password","Updating user password. User: " + user.getId());
        String pwd = password.getPassword();
        boolean isValidPassword = passwordValidator.test(password.getPassword());
        if (!isValidPassword)
            throw new IllegalStateException("Hasło nie spełnia wymagań bezpieczeństwa");
        user.setPassword(bCryptPasswordEncoder.encode(pwd));
        userRepository.save(user);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hasło zostało zaktualizowane");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    private void transferUserAssetsToDefaultAdmin(Long userId, User defaultAdmin) {
        List<Post> posts = postService.getAllPostsByUser(userId);
        for (Post post : posts) {
            post.setAuthorId(defaultAdmin);
            postRepository.save(post);
        }

        List<Rent> rents = rentRepository.findByUserId(userId);
        for (Rent rent : rents) {
            rent.setUser(defaultAdmin);
            rentRepository.save(rent);
        }

        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        for (Purchase purchase : purchases) {
            purchase.setUser(defaultAdmin);
            purchaseRepository.save(purchase);
        }

        List<UserPass> userPasses = userPassRepository.findByUserId(userId);
        for (UserPass userPass : userPasses) {
            userPass.setUser(defaultAdmin);
            userPassRepository.save(userPass);
        }
    }
}
