package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.RentalContractRequest;
import rentalpropertymanagementapp.be.DTO.RentalContractResponse;
import rentalpropertymanagementapp.be.Service.RentalContractService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/contracts")
public class RentalContractController {

    private final RentalContractService service;

    // Khởi tạo Controller và tiêm interface quản lý hợp đồng thuê.
    public RentalContractController(RentalContractService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Danh sách hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả hợp đồng thuê.
     * Chuyển từng entity RentalContract thành RentalContractResponse trước khi trả về.
     *
     * GET /be/contracts
     */
    @GetMapping
    public List<RentalContractResponse> list() {
        return service.getAll()
                .stream()
                .map(RentalContractResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Chi tiết hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin chi tiết hợp đồng theo UUID.
     * Nếu không tìm thấy hợp đồng, service sẽ xử lý ngoại lệ tương ứng.
     *
     * GET /be/contracts/{id}
     */
    @GetMapping("/{id}")
    public RentalContractResponse get(@PathVariable UUID id) {
        return RentalContractResponse.from(
                service.get(id)
        );
    }

    // -------------------------------------------------------------------------
    // Tạo hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Tạo hợp đồng thuê mới và nhận tài liệu đính kèm nếu có.
     * Dữ liệu hợp đồng được gửi trong phần "contract";
     * tài liệu được gửi trong phần "document".
     * Trả về HTTP 201 Created khi tạo thành công.
     *
     * POST /be/contracts
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RentalContractResponse> create(
            @Valid @RequestPart("contract") RentalContractRequest request,
            @RequestPart(
                    value = "document",
                    required = false
            ) MultipartFile document
    ) {
        RentalContractResponse response = RentalContractResponse.from(
                service.create(request, document)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // -------------------------------------------------------------------------
    // Cập nhật hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin hợp đồng theo UUID.
     * Nhận dữ liệu hợp đồng và tài liệu mới nếu được gửi lên.
     * Trả về thông tin hợp đồng sau khi cập nhật.
     *
     * PUT /be/contracts/{id}
     * Content-Type: multipart/form-data
     */
    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public RentalContractResponse update(
            @PathVariable UUID id,
            @Valid @RequestPart("contract") RentalContractRequest request,
            @RequestPart(
                    value = "document",
                    required = false
            ) MultipartFile document
    ) {
        return RentalContractResponse.from(
                service.update(id, request, document)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa hợp đồng
    // -------------------------------------------------------------------------

    /**
     * Xóa hợp đồng thuê theo UUID.
     * Nếu hợp đồng có dữ liệu phụ thuộc, service có thể từ chối xóa.
     * Trả về HTTP 204 No Content nếu xóa thành công.
     *
     * DELETE /be/contracts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}