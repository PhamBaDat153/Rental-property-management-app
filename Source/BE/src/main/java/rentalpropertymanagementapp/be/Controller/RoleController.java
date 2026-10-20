package rentalpropertymanagementapp.be.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/be/roles")
public class RoleController {

    private final RoleService roleService;

    // Khởi tạo Controller và tiêm service quản lý vai trò.
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Lấy danh sách tất cả vai trò trong hệ thống.
     * Trả về HTTP 200 OK cùng danh sách Role.
     *
     * GET /be/roles
     */
    @GetMapping
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }
}