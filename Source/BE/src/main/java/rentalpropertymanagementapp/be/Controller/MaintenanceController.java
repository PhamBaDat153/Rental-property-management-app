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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.DTO.MaintenanceResponse;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Service.MaintenanceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/maintenance")
public class MaintenanceController {

    private final MaintenanceService service;

    // Khởi tạo Controller và tiêm interface quản lý yêu cầu bảo trì.
    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Maintenance list
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách yêu cầu bảo trì.
     * Có thể lọc theo phòng, trạng thái và mức độ ưu tiên.
     * Chuyển các entity thành MaintenanceResponse trước khi trả về.
     *
     * GET /be/maintenance
     */
    @GetMapping
    public List<MaintenanceResponse> list(
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) String priority
    ) {
        return service.list(roomId, status, priority)
                .stream()
                .map(service::response)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Maintenance detail
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin chi tiết của một yêu cầu bảo trì theo UUID.
     *
     * GET /be/maintenance/{id}
     */
    @GetMapping("/{id}")
    public MaintenanceResponse get(@PathVariable UUID id) {
        return service.response(service.get(id));
    }

    // -------------------------------------------------------------------------
    // Create maintenance request
    // -------------------------------------------------------------------------

    /**
     * Tạo yêu cầu bảo trì mới từ dữ liệu gửi lên.
     * Kiểm tra dữ liệu bằng Bean Validation.
     * Trả về HTTP 201 Created khi tạo thành công.
     *
     * POST /be/maintenance
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse create(
            @Valid
            @RequestBody
            rentalpropertymanagementapp.be.DTO.MaintenanceRequest request
    ) {
        return service.response(
                service.save(null, request)
        );
    }

    // -------------------------------------------------------------------------
    // Update maintenance request
    // -------------------------------------------------------------------------

    /**
     * Cập nhật yêu cầu bảo trì theo UUID.
     * Nhận thông tin cập nhật từ request body và trả về dữ liệu sau khi lưu.
     *
     * PUT /be/maintenance/{id}
     */
    @PutMapping("/{id}")
    public MaintenanceResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            rentalpropertymanagementapp.be.DTO.MaintenanceRequest request
    ) {
        return service.response(
                service.save(id, request)
        );
    }

    // -------------------------------------------------------------------------
    // Delete maintenance request
    // -------------------------------------------------------------------------

    /**
     * Xóa yêu cầu bảo trì theo UUID.
     * Trả về HTTP 204 No Content nếu xóa thành công.
     *
     * DELETE /be/maintenance/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}