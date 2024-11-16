package pwr.isa.klama.auth;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class PasswordValidator implements Predicate<String> {
    /**
     * Evaluates this predicate on the given argument. Password must contain one digit from 0 to 9, one lowercase letter, one uppercase letter, one special character, no space, and it must be at least characters long.
     *
     * @param s the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(String s) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,}$";
        return s.matches(passwordRegex);
    }
}
