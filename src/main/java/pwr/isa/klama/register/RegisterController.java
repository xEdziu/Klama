package pwr.isa.klama.register;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/register")
@AllArgsConstructor
public class RegisterController {

    private RegisterService registerService;

    @PostMapping
    public String register(@RequestBody RegisterRequest request){
        return registerService.register(request);
    }
}
