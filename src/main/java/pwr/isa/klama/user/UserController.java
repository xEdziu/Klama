package pwr.isa.klama.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pwr.isa.klama.exceptions.ForbiddenActionException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/authorized")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/user")
    public UserDTO getUserInfo() {
        return userService.getUserInfo();
    }

    @PutMapping(path = "/user/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    // This method is both for updating password of the user and for the admin
    @PutMapping(path = "/user/update/password")
    public Map<String, Object> updateUserPassword(@RequestBody User password) {
        return userService.updateUserPassword(password);
    }

    @DeleteMapping(path = "/user/delete")
    public Map<String, Object> deleteUser() {
        return userService.deleteUser();
    }

    @GetMapping(path = "/admin/user/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(path = "/admin/user/{userId}")
    public UserDetails getUserByIdAdmin(@PathVariable("userId") Long userId) {
        return userService.loadUserById(userId);
    }

    @PutMapping(path = "/admin/user/update/{userId}")
    public Map<String, Object> updateUserAdmin(@PathVariable("userId") Long userId, @RequestBody User user) {
        return userService.updateUserAdmin(userId, user);
    }

    @DeleteMapping(path = "/admin/user/delete/{userId}")
    public Map<String, Object> deleteUserAdmin(@PathVariable("userId") Long userId) {
        if (userId == 1) {
            throw new ForbiddenActionException("Cannot delete default admin account");
        }
        return userService.deleteUserAdmin(userId);
    }

    @PostMapping(path = "/admin/user/create")
    public Map<String, Object> createUserAdmin(@RequestBody User user) {
        return userService.createUserAdmin(user);
    }
}
