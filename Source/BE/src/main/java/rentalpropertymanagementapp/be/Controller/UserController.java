package rentalpropertymanagementapp.be.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserUpdateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/be/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/manage")
    public ResponseEntity<List<UserTenantResponse>> getUsers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getUsers(search));
    }

    @GetMapping("/manage/{id}")
    public ResponseEntity<UserTenantResponse> getUserByID(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserByID(id));
    }

    @PostMapping("/manage")
    public ResponseEntity<UserTenantResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @DeleteMapping("/manage/{id}")
    public ResponseEntity<Void> deleteManagedUser(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/manage/{id}")
    public ResponseEntity<UserTenantResponse> updateManagedUser(
            @PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserByID(id, request));
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
