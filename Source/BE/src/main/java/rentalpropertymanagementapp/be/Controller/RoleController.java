package rentalpropertymanagementapp.be.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Repository.RoleRepository;
import rentalpropertymanagementapp.be.Service.RoleService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/be/roles")
public class RoleController {

    @Autowired
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }

}
