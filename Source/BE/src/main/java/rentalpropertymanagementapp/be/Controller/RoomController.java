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

import rentalpropertymanagementapp.be.DTO.RoomRequest;
import rentalpropertymanagementapp.be.DTO.RoomResponse;
import rentalpropertymanagementapp.be.Service.RoomService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/rooms")
public class RoomController {

    private final RoomService service;

    // Khởi tạo Controller và tiêm interface quản lý phòng.
    public RoomController(RoomService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Danh sách phòng
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả phòng trong hệ thống.
     * Chuyển mỗi entity Room thành RoomResponse trước khi trả về.
     *
     * GET /be/rooms
     */
    @GetMapping
    public List<RoomResponse> list() {
        return service.getAll()
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Chi tiết phòng
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin chi tiết phòng theo UUID.
     * Nếu phòng không tồn tại, service sẽ xử lý ngoại lệ tương ứng.
     *
     * GET /be/rooms/{id}
     */
    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable UUID id) {
        return RoomResponse.from(
                service.get(id)
        );
    }

    // -------------------------------------------------------------------------
    // Tạo phòng
    // -------------------------------------------------------------------------

    /**
     * Tạo phòng mới với thông tin phòng và danh sách ảnh tùy chọn.
     * Dữ liệu phòng được gửi trong phần "room";
     * ảnh phòng được gửi trong phần "images".
     * Trả về HTTP 201 Created khi tạo thành công.
     *
     * POST /be/rooms
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RoomResponse> create(
            @Valid @RequestPart("room") RoomRequest request,
            @RequestPart(
                    value = "images",
                    required = false
            ) MultipartFile[] images
    ) {
        RoomResponse response = RoomResponse.from(
                service.create(request, images)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // -------------------------------------------------------------------------
    // Cập nhật phòng
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin phòng theo UUID.
     * Nhận dữ liệu phòng và danh sách ảnh mới nếu có.
     * Trả về thông tin phòng sau khi cập nhật.
     *
     * PUT /be/rooms/{id}
     * Content-Type: multipart/form-data
     */
    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public RoomResponse update(
            @PathVariable UUID id,
            @Valid @RequestPart("room") RoomRequest request,
            @RequestPart(
                    value = "images",
                    required = false
            ) MultipartFile[] images
    ) {
        return RoomResponse.from(
                service.update(id, request, images)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa phòng
    // -------------------------------------------------------------------------

    /**
     * Xóa phòng theo UUID.
     * Nếu phòng có hợp đồng thuê liên quan, service có thể từ chối xóa.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/rooms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}