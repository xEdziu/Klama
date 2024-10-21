package pwr.isa.klama.security.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<List<String>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        List<String> errors = List.of(ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<List<String>> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        List<String> errors = List.of(ex.getMessage(), String.valueOf(HttpStatus.NOT_FOUND.value()));
        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }
    // Add other exception handlers as needed
}