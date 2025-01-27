package pwr.isa.klama.auth.registration;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/register")
@AllArgsConstructor
public class RegisterController {

    private RegisterService registerService;

    @PostMapping(path="/signup")
    public Map<String, Object> register(@RequestBody RegisterRequest request){
        return registerService.register(request);
    }

    @GetMapping(path="/confirm")
    public Map<String, Object> confirm(@RequestParam("token") String token){
        return registerService.confirmToken(token);
    }
}
