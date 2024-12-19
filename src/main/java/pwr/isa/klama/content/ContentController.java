package pwr.isa.klama.content;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContentController {

    @GetMapping("/")
    public String getIndex() {
        System.out.println("ContentController.getIndex -> index");
        return "index";
    }

    @GetMapping("/home")
    public String getHome() {
        System.out.println("ContentController.getHome -> home");
        return "home/home";
    }

    @GetMapping("/admin")
    public String getAdmin() {
        System.out.println("ContentController.getAdmin -> admin");
        return "admin/admin";
    }

    @GetMapping("/login")
    public String getLogin() {
        System.out.println("ContentController.getLogin -> login");
        return "auth/login";
    }

    @GetMapping("/shop")
    public String getShop() {
        System.out.println("ContentController.getLogin -> shop");
        return "shop/shop";
    }

    @GetMapping("/rent")
    public String getRent() {
        System.out.println("ContentController.getLogin -> rent");
        return "rent/rent";
    }

    @GetMapping("/blog")
    public String getBlog() {
        System.out.println("ContentController.getLogin -> blog");
        return "blog/blog";
    }
}
