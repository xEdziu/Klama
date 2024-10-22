package pwr.isa.klama.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/user/{userId}")
    public UserDTO getUserById(@PathVariable("userId") Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping(path = "/user/update/{userId}")
    public Map<String, Object> updateUser(@PathVariable("userId") Long userId, @RequestBody User user) {
        return userService.updateUser(userId, user);
    }

    @DeleteMapping(path = "/user/delete/{userId}")
    public Map<String, Object> deleteUser(@PathVariable("userId") Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping(path = "/admin/user/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(path = "/admin/user/{userId}l")
    public UserDetails getUserByIdAdmin(@PathVariable("userId") Long userId) {
        return userService.loadUserById(userId);
    }

    @PutMapping(path = "/admin/user/update/{userId}")
    public Map<String, Object> updateUserAdmin(@PathVariable("userId") Long userId, @RequestBody User user) {
        return userService.updateUserAdmin(userId, user);
    }

    @DeleteMapping(path = "/admin/user/delete/{userId}")
    public Map<String, Object> deleteUserAdmin(@PathVariable("userId") Long userId) {
        return userService.deleteUserAdmin(userId);
    }
}