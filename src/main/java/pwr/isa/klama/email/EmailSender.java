package pwr.isa.klama.email;

public interface EmailSender {
    void send(String to, String message, String subject);
}
