package pwr.isa.klama.security.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.sql.Timestamp;
import java.util.Date;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        String message = ex.getMessage();
        String errorFromHttp = HttpStatus.BAD_REQUEST.getReasonPhrase();
        String requestUri = request.getDescription(false);
        String errorJson = "{\"timestamp\":\"" + timestamp + "\",\"message\":\"" + message + "\",\"error\":\"" + errorFromHttp + "\",\"status\":\"" + HttpStatus.BAD_REQUEST.value() + "\",\"uri\":\"" + requestUri + "\"}";
        return new ResponseEntity<>(errorJson, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        String message = ex.getMessage();
        String errorFromHttp = HttpStatus.NOT_FOUND.getReasonPhrase();
        String requestUri = request.getDescription(false);
        String errorJson = "{\"timestamp\":\"" + timestamp + "\",\"message\":\"" + message + "\",\"error\":\"" + errorFromHttp + "\",\"status\":\"" + HttpStatus.NOT_FOUND.value() + "\",\"uri\":\"" + requestUri + "\"}";
        return new ResponseEntity<>(errorJson, HttpStatus.NOT_FOUND);
    }
    // Add other exception handlers as needed
}