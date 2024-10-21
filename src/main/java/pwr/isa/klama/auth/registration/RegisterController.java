package pwr.isa.klama.auth.registration;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/register")
@AllArgsConstructor
public class RegisterController {

    private RegisterService registerService;

    @PostMapping(path="/signup")
    public String register(@RequestBody RegisterRequest request){
        return registerService.register(request);
    }

    @PostMapping(path="/confirm")
    public String confirm(@RequestParam("token") String token){
        return registerService.confirmToken(token);
    }
}
