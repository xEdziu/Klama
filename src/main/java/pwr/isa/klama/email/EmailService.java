package pwr.isa.klama.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pwr.isa.klama.security.logging.ApiLogger;

@Service
@AllArgsConstructor
public class EmailService implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void send(String to, String message, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, "utf-8"
            );
            helper.setText(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("adrian.goral@gmail.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            ApiLogger.logSevere("EmailService.class","failed to send email" + e);
            throw new IllegalStateException("Nie udało się wysłać emaila");
        }
    }
}
