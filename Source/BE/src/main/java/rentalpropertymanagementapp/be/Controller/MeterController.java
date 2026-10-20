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

import rentalpropertymanagementapp.be.DTO.MeterReadingResponse;
import rentalpropertymanagementapp.be.DTO.MeterRequest;
import rentalpropertymanagementapp.be.DTO.MeterResponse;
import rentalpropertymanagementapp.be.Service.MeterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/meters")
public class MeterController {

    private final MeterService service;

    // Khởi tạo Controller và tiêm interface quản lý công tơ.
    public MeterController(MeterService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Danh sách công tơ
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách công tơ thuộc một phòng theo roomId.
     * Chuyển từng entity Meter thành MeterResponse trước khi trả về.
     *
     * GET /be/meters?roomId={roomId}
     */
    @GetMapping
    public List<MeterResponse> list(
            @RequestParam UUID roomId
    ) {
        return service.listMeters(roomId)
                .stream()
                .map(MeterResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Chi tiết công tơ
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin công tơ theo UUID.
     * Nếu không tìm thấy công tơ, service sẽ xử lý ngoại lệ tương ứng.
     *
     * GET /be/meters/{id}
     */
    @GetMapping("/{id}")
    public MeterResponse get(
            @PathVariable UUID id
    ) {
        return MeterResponse.from(
                service.getMeter(id)
        );
    }

    // -------------------------------------------------------------------------
    // Tạo công tơ
    // -------------------------------------------------------------------------

    /**
     * Tạo công tơ mới từ dữ liệu gửi lên.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created khi tạo thành công.
     *
     * POST /be/meters
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeterResponse create(
            @Valid @RequestBody MeterRequest request
    ) {
        return MeterResponse.from(
                service.createMeter(request)
        );
    }

    // -------------------------------------------------------------------------
    // Cập nhật công tơ
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin công tơ theo UUID.
     * Dữ liệu đầu vào được kiểm tra bằng Bean Validation.
     * Trả về thông tin công tơ sau khi cập nhật.
     *
     * PUT /be/meters/{id}
     */
    @PutMapping("/{id}")
    public MeterResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody MeterRequest request
    ) {
        return MeterResponse.from(
                service.updateMeter(id, request)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa công tơ
    // -------------------------------------------------------------------------

    /**
     * Xóa công tơ theo UUID.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/meters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        service.deleteMeter(id);

        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Danh sách chỉ số công tơ
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách các lần ghi chỉ số của một công tơ.
     * Chuyển từng entity MeterReading thành MeterReadingResponse trước khi trả về.
     *
     * GET /be/meters/{meterId}/readings
     */
    @GetMapping("/{meterId}/readings")
    public List<MeterReadingResponse> readings(
            @PathVariable UUID meterId
    ) {
        return service.listReadings(meterId)
                .stream()
                .map(MeterReadingResponse::from)
                .toList();
    }
}