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

import rentalpropertymanagementapp.be.DTO.InvoiceRequest;
import rentalpropertymanagementapp.be.DTO.InvoiceResponse;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import rentalpropertymanagementapp.be.Service.InvoiceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/invoices")
public class InvoiceController {

    private final InvoiceService service;

    // Khởi tạo Controller và tiêm interface quản lý hóa đơn.
    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Invoice list
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách hóa đơn.
     * Có thể lọc theo mã hợp đồng và trạng thái hóa đơn.
     * Chuyển từng entity Invoice thành InvoiceResponse trước khi trả về.
     *
     * GET /be/invoices
     */
    @GetMapping
    public List<InvoiceResponse> list(
            @RequestParam(required = false) UUID contractId,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        return service.list(contractId, status)
                .stream()
                .map(service::response)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Invoice detail
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin chi tiết hóa đơn theo UUID.
     *
     * GET /be/invoices/{id}
     */
    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable UUID id) {
        return service.response(service.get(id));
    }

    // -------------------------------------------------------------------------
    // Create invoice
    // -------------------------------------------------------------------------

    /**
     * Tạo hóa đơn mới từ dữ liệu gửi lên.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created khi tạo thành công.
     *
     * POST /be/invoices
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse create(
            @Valid @RequestBody InvoiceRequest request
    ) {
        return service.response(
                service.save(null, request)
        );
    }

    // -------------------------------------------------------------------------
    // Update invoice
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin hóa đơn theo UUID.
     * Nhận dữ liệu cập nhật từ request body và trả về hóa đơn sau khi lưu.
     *
     * PUT /be/invoices/{id}
     */
    @PutMapping("/{id}")
    public InvoiceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceRequest request
    ) {
        return service.response(
                service.save(id, request)
        );
    }

    // -------------------------------------------------------------------------
    // Delete invoice
    // -------------------------------------------------------------------------

    /**
     * Xóa hóa đơn theo UUID.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/invoices/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}