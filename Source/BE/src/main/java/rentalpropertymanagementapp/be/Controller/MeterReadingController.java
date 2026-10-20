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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.MeterReadingRequest;
import rentalpropertymanagementapp.be.DTO.MeterReadingResponse;
import rentalpropertymanagementapp.be.Service.MeterService;

import java.util.UUID;

@RestController
@RequestMapping("/be/meter-readings")
public class MeterReadingController {

    private final MeterService service;

    // Khởi tạo Controller và tiêm interface quản lý công tơ, chỉ số công tơ.
    public MeterReadingController(MeterService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Get meter reading
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin một lần ghi chỉ số công tơ theo UUID.
     * Trả về dữ liệu dưới dạng MeterReadingResponse.
     *
     * GET /be/meter-readings/{id}
     */
    @GetMapping("/{id}")
    public MeterReadingResponse get(@PathVariable UUID id) {
        return MeterReadingResponse.from(
                service.getReading(id)
        );
    }

    // -------------------------------------------------------------------------
    // Create meter reading
    // -------------------------------------------------------------------------

    /**
     * Tạo lần ghi chỉ số công tơ mới bằng JSON.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created nếu tạo thành công.
     *
     * POST /be/meter-readings
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeterReadingResponse create(
            @Valid @RequestBody MeterReadingRequest request
    ) {
        return MeterReadingResponse.from(
                service.createReading(request)
        );
    }

    /**
     * Tạo lần ghi chỉ số công tơ mới kèm ảnh minh chứng tùy chọn.
     * Dữ liệu chỉ số được gửi trong phần "reading";
     * ảnh minh chứng được gửi trong phần "evidence".
     * Trả về HTTP 201 Created nếu tạo thành công.
     *
     * POST /be/meter-readings
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public MeterReadingResponse createMultipart(
            @Valid @RequestPart("reading") MeterReadingRequest request,
            @RequestPart(
                    value = "evidence",
                    required = false
            ) MultipartFile evidence
    ) {
        return MeterReadingResponse.from(
                service.createReading(request, evidence)
        );
    }

    // -------------------------------------------------------------------------
    // Update meter reading
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin lần ghi chỉ số bằng JSON.
     * Nhận UUID của lần ghi chỉ số và dữ liệu cập nhật trong request body.
     *
     * PUT /be/meter-readings/{id}
     */
    @PutMapping("/{id}")
    public MeterReadingResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody MeterReadingRequest request
    ) {
        return MeterReadingResponse.from(
                service.updateReading(id, request)
        );
    }

    /**
     * Cập nhật lần ghi chỉ số bằng multipart/form-data.
     * Phần "reading" chứa dữ liệu chỉ số; phần "evidence" chứa ảnh minh chứng
     * mới nếu được gửi lên.
     *
     * PUT /be/meter-readings/{id}
     * Content-Type: multipart/form-data
     */
    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public MeterReadingResponse updateMultipart(
            @PathVariable UUID id,
            @Valid @RequestPart("reading") MeterReadingRequest request,
            @RequestPart(
                    value = "evidence",
                    required = false
            ) MultipartFile evidence
    ) {
        return MeterReadingResponse.from(
                service.updateReading(id, request, evidence)
        );
    }

    // -------------------------------------------------------------------------
    // Delete meter reading
    // -------------------------------------------------------------------------

    /**
     * Xóa lần ghi chỉ số công tơ theo UUID.
     * Nếu xóa thành công, trả về HTTP 204 No Content.
     * Nếu chỉ số đã được sử dụng để lập hóa đơn, service sẽ xử lý từ chối xóa.
     *
     * DELETE /be/meter-readings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteReading(id);

        return ResponseEntity.noContent().build();
    }
}