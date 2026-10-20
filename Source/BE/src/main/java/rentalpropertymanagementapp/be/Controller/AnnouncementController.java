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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.DTO.AnnouncementRequest;
import rentalpropertymanagementapp.be.DTO.AnnouncementResponse;
import rentalpropertymanagementapp.be.Service.AnnouncementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    // Khởi tạo Controller và tiêm interface quản lý thông báo.
    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Danh sách thông báo
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả thông báo.
     * Chuyển từng entity Announcement thành AnnouncementResponse trước khi trả về.
     *
     * GET /be/announcements
     */
    @GetMapping
    public List<AnnouncementResponse> list() {
        return service.list()
                .stream()
                .map(service::response)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Tạo thông báo
    // -------------------------------------------------------------------------

    /**
     * Tạo thông báo mới từ dữ liệu request.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created nếu tạo thành công.
     *
     * POST /be/announcements
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse create(
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return service.response(
                service.save(null, request)
        );
    }

    // -------------------------------------------------------------------------
    // Cập nhật thông báo
    // -------------------------------------------------------------------------

    /**
     * Cập nhật nội dung, loại thông báo, trạng thái gửi
     * và danh sách người nhận theo UUID.
     * Trả về thông tin thông báo sau khi cập nhật.
     *
     * PUT /be/announcements/{id}
     */
    @PutMapping("/{id}")
    public AnnouncementResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return service.response(
                service.save(id, request)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa thông báo
    // -------------------------------------------------------------------------

    /**
     * Xóa thông báo theo UUID.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/announcements/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}