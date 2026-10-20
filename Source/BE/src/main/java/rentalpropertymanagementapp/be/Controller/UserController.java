package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.DTO.LoginRequest;
import rentalpropertymanagementapp.be.DTO.LoginResponse;
import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserProfileUpdateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.DTO.UserUpdateRequest;
import rentalpropertymanagementapp.be.Service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/user")
public class UserController {

    private final UserService userService;

    // Khởi tạo Controller và tiêm interface quản lý người dùng.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // Quản lý người dùng
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách người dùng và hồ sơ người thuê.
     * Có thể truyền từ khóa search để lọc kết quả.
     *
     * GET /be/user/manage?search=keyword
     */
    @GetMapping("/manage")
    public ResponseEntity<List<UserTenantResponse>> getUsers(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                userService.getUsers(search)
        );
    }

    /**
     * Lấy thông tin chi tiết người dùng theo UUID.
     *
     * GET /be/user/manage/{id}
     */
    @GetMapping("/manage/{id}")
    public ResponseEntity<UserTenantResponse> getUserByID(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                userService.getUserByID(id)
        );
    }

    /**
     * Tạo tài khoản người dùng mới.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created cùng thông tin tài khoản vừa tạo.
     *
     * POST /be/user/manage
     */
    @PostMapping("/manage")
    public ResponseEntity<UserTenantResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserTenantResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Xóa tài khoản người dùng theo UUID.
     * Nếu xóa thành công, trả về HTTP 204 No Content.
     *
     * DELETE /be/user/manage/{id}
     */
    @DeleteMapping("/manage/{id}")
    public ResponseEntity<Void> deleteManagedUser(
            @PathVariable UUID id
    ) {
        userService.deleteUserById(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật vai trò và trạng thái tài khoản người dùng.
     * Dữ liệu đầu vào được kiểm tra bằng Bean Validation.
     *
     * PUT /be/user/manage/{id}
     */
    @PutMapping("/manage/{id}")
    public ResponseEntity<UserTenantResponse> updateManagedUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUserByID(id, request)
        );
    }

    /**
     * Cập nhật hồ sơ cá nhân của người thuê.
     * Bao gồm các thông tin hồ sơ được định nghĩa trong UserProfileUpdateRequest.
     *
     * PUT /be/user/manage/{id}/profile
     */
    @PutMapping("/manage/{id}/profile")
    public ResponseEntity<UserTenantResponse> updateUserProfile(
            @PathVariable UUID id,
            @RequestBody UserProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUserProfile(id, request)
        );
    }

    // -------------------------------------------------------------------------
    // Đăng nhập
    // -------------------------------------------------------------------------

    /**
     * Xác thực thông tin đăng nhập.
     * Nếu thông tin hợp lệ và người dùng được phép đăng nhập theo loginType,
     * trả về LoginResponse với HTTP 200 OK.
     * Nếu xác thực thất bại, trả về HTTP 401 Unauthorized.
     *
     * POST /be/user/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.authenticate(
                        request.username(),
                        request.password(),
                        request.loginType()
                )
                .map(LoginResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .build()
                );
    }
}