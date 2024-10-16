package pwr.isa.klama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

@SpringBootApplication
@RestController
public class KlamaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KlamaApplication.class, args);
    }

    @GetMapping("/hello")
    public List<String> hello() {
        return List.of(
                "Hello, World!",
                "Current time: " + new Timestamp(new Date().getTime())
        );
    }
}
