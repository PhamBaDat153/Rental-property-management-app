package rentalpropertymanagementapp.be.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/be/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/login")
    public ResponseEntity<User> Login(@RequestParam(required = true) String username,
                                      @RequestParam(required = true) String password,
                                      @RequestParam(required = true) LoginType loginType) {
        Optional<User> user = userService.authenticate(username, password, loginType);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
