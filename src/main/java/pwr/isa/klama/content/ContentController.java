package pwr.isa.klama.content;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pwr.isa.klama.user.UserRole;

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

    // admin Rental services
    @GetMapping("/addRentalItem")
    public String getAddRentalItem() {
        System.out.println("ContentController.getAddRentalItem -> addRentalItem");
        return "admin/rental/addRentalItem";
    }

    @GetMapping("/editRentalItem")
    public String getEditRentalItem() {
        System.out.println("ContentController.getEditRentalItem -> editRentalItem");
        return "admin/rental/editRentalItem";
    }

    @GetMapping("/allRentalItems")
    public String getAllRentalItems() {
        System.out.println("ContentController.getAllRentalItems -> allRentalItems");
        return "admin/rental/allRentalItems";
    }

    // admin Shop services
    @GetMapping("/addShopItem")
    public String getAddShopItem() {
        System.out.println("ContentController.getAddShopItem -> addShopItem");
        return "admin/shop/addShopItem";
    }

    @GetMapping("/editShopItem")
    public String getEditShopItem() {
        System.out.println("ContentController.getEditShopItem -> editShopItem");
        return "admin/shop/editShopItem";
    }

    @GetMapping("/allShopItems")
    public String getAllShopItems() {
        System.out.println("ContentController.getAllShopItems -> allShopItems");
        return "admin/shop/allshopItems";
    }

    // admin Pass services
    @GetMapping("/addPass")
    public String getAddPass() {
        System.out.println("ContentController.getAddPass -> addPass");
        return "admin/pass/addPass";
    }

    @GetMapping("/editPass")
    public String getEditPass() {
        System.out.println("ContentController.getEditPass -> editPass");
        return "admin/pass/editPass";
    }

    @GetMapping("/allPasses")
    public String getAllPasses() {
        System.out.println("ContentController.getAllPasses -> allPasses");
        return "admin/pass/allPasses";
    }



    @GetMapping("/login")
    public String getLogin() {
        Authentication authentication = getAuthentication();

        if (authentication.isAuthenticated() && authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            System.out.println("ContentController.getLogin -> admin");
            return "redirect:/admin";
        } else if (authentication.isAuthenticated() && authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.ROLE_USER.name()))) {
            System.out.println("ContentController.getLogin -> home");
            return "redirect:/home";
        }

        System.out.println("ContentController.getLogin -> login");
        return "auth/login";
    }

    @GetMapping("/shop")
    public String getShop() {
        System.out.println("ContentController.getLogin -> shop");
        return "auth/shop";
    }

    @GetMapping("/rent")
    public String getRent() {
        System.out.println("ContentController.getLogin -> rent");
        return "auth/rent";
    }

    @GetMapping("/blog")
    public String getBlog() {
        System.out.println("ContentController.getLogin -> blog");
        return "blog";
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
