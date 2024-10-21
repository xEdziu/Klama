package pwr.isa.klama.user;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.auth.registration.token.ConfirmationToken;
import pwr.isa.klama.auth.registration.token.ConfirmationTokenService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG_USERNAME = "User with username %s not found";
    private final static String USER_NOT_FOUND_MSG_EMAIL = "User with email %s not found";
    private final static String USER_NOT_FOUND_MSG_USERNAME_OR_EMAIL = "User with username %s or email %s not found";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG_USERNAME, username)));
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG_EMAIL, email)));
    }

    public UserDetails loadUserByUsernameOrEmail(String username, String email) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username, email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG_USERNAME_OR_EMAIL, username, email)));
    }

    public String signUpUser(User user) {
        boolean userExists = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()).isPresent();
        if (userExists) {
            throw new IllegalStateException("Username or email already taken");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                new Timestamp(new Date().getTime()),
                // 15 minutes
                new Timestamp(new Date().getTime() + 1000 * 60 * 15),
                user
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //TODO: Send email
        return token;
    }
}
