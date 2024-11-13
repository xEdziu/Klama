package pwr.isa.klama.security.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import pwr.isa.klama.exceptions.AccountNotActivatedException;
import pwr.isa.klama.exceptions.ForbiddenActionException;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.security.logging.ApiLogger;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ApiLogger.logWarning(request.getDescription(false).substring(4), ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("message", ex.getMessage());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("uri", request.getDescription(false).substring(4));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ApiLogger.logWarning(request.getDescription(false).substring(4), ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("message", ex.getMessage());
        response.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("uri", request.getDescription(false).substring(4));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotActivated(AccountNotActivatedException ex, WebRequest request) {
        ApiLogger.logWarning(request.getDescription(false).substring(4), ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("message", ex.getMessage());
        response.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("uri", request.getDescription(false).substring(4));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenAction(ForbiddenActionException ex, WebRequest request) {
        ApiLogger.logSevere(request.getDescription(false).substring(4), ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Timestamp(new Date().getTime()));
        response.put("message", ex.getMessage());
        response.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("uri", request.getDescription(false).substring(4));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Add other exception handlers as needed
}