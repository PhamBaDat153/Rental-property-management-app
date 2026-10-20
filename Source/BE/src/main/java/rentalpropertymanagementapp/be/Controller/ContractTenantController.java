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
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.DTO.ContractTenantResponse;
import rentalpropertymanagementapp.be.Service.ContractTenantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/contract-tenants")
public class ContractTenantController {

    private final ContractTenantService service;

    // Khởi tạo Controller và tiêm interface quản lý người thuê trong hợp đồng.
    public ContractTenantController(ContractTenantService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Danh sách người thuê trong hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả bản ghi liên kết giữa người thuê và hợp đồng.
     * Chuyển từng entity ContractTenant thành ContractTenantResponse trước khi trả về.
     *
     * GET /be/contract-tenants
     */
    @GetMapping
    public List<ContractTenantResponse> list() {
        return service.getAll()
                .stream()
                .map(ContractTenantResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Chi tiết người thuê trong hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin liên kết giữa một hợp đồng và một người thuê.
     * Sử dụng contractId và tenantId để xác định bản ghi cần truy vấn.
     *
     * GET /be/contract-tenants/{contractId}/{tenantId}
     */
    @GetMapping("/{contractId}/{tenantId}")
    public ContractTenantResponse get(
            @PathVariable UUID contractId,
            @PathVariable UUID tenantId
    ) {
        return ContractTenantResponse.from(
                service.get(contractId, tenantId)
        );
    }

    // -------------------------------------------------------------------------
    // Thêm người thuê vào hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Tạo liên kết giữa người thuê và hợp đồng từ dữ liệu request.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created cùng thông tin liên kết vừa tạo.
     *
     * POST /be/contract-tenants
     */
    @PostMapping
    public ResponseEntity<ContractTenantResponse> create(
            @Valid @RequestBody ContractTenantRequest request
    ) {
        ContractTenantResponse response = ContractTenantResponse.from(
                service.create(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // -------------------------------------------------------------------------
    // Cập nhật thông tin người thuê trong hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin liên kết theo contractId và tenantId.
     * Dữ liệu request được kiểm tra trước khi chuyển đến service xử lý.
     * Trả về thông tin liên kết sau khi cập nhật thành công.
     *
     * PUT /be/contract-tenants/{contractId}/{tenantId}
     */
    @PutMapping("/{contractId}/{tenantId}")
    public ContractTenantResponse update(
            @PathVariable UUID contractId,
            @PathVariable UUID tenantId,
            @Valid @RequestBody ContractTenantRequest request
    ) {
        return ContractTenantResponse.from(
                service.update(contractId, tenantId, request)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa người thuê khỏi hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Xóa liên kết giữa người thuê và hợp đồng theo hai UUID.
     * Không trực tiếp xóa tài khoản người dùng hoặc hồ sơ người thuê.
     * Trả về HTTP 204 No Content nếu xóa thành công.
     *
     * DELETE /be/contract-tenants/{contractId}/{tenantId}
     */
    @DeleteMapping("/{contractId}/{tenantId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID contractId,
            @PathVariable UUID tenantId
    ) {
        service.delete(contractId, tenantId);

        return ResponseEntity.noContent().build();
    }
}