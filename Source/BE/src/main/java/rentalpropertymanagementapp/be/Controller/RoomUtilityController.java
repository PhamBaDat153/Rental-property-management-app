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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import rentalpropertymanagementapp.be.DTO.RoomServiceResponse;
import rentalpropertymanagementapp.be.DTO.RoomServiceStatusRequest;
import rentalpropertymanagementapp.be.DTO.ServiceRequest;
import rentalpropertymanagementapp.be.DTO.ServiceResponse;
import rentalpropertymanagementapp.be.Service.RoomUtilityService;


import java.util.List;
import java.util.UUID;

@RestController
public class RoomUtilityController {

    private final RoomUtilityService service;

    /**
     * Khởi tạo Controller và tiêm interface quản lý dịch vụ phòng.
     */
    public RoomUtilityController(RoomUtilityService service) {
        this.service = service;
    }

    /**
     * Lấy danh sách tất cả dịch vụ trong danh mục.
     * Chuyển từng đối tượng Service thành ServiceResponse trước khi trả về.
     *
     * GET /be/services
     */
    @GetMapping("/be/services")
    public List<ServiceResponse> services() {
        return service.listServices()
                .stream()
                .map(ServiceResponse::from)
                .toList();
    }

    /**
     * Tạo dịch vụ mới trong danh mục.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về thông tin dịch vụ vừa tạo với HTTP 201 Created.
     *
     * POST /be/services
     */
    @PostMapping("/be/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(
            @Valid @RequestBody ServiceRequest request
    ) {
        return ServiceResponse.from(
                service.createService(request)
        );
    }

    /**
     * Cập nhật thông tin dịch vụ theo UUID.
     * Nhận dữ liệu cập nhật từ request body và trả về dịch vụ sau khi cập nhật.
     *
     * PUT /be/services/{id}
     */
    @PutMapping("/be/services/{id}")
    public ServiceResponse updateService(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequest request
    ) {
        return ServiceResponse.from(
                service.updateService(id, request)
        );
    }

    /**
     * Xóa dịch vụ khỏi danh mục theo UUID.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/services/{id}
     */
    @DeleteMapping("/be/services/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable UUID id
    ) {
        service.deleteService(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách dịch vụ được gán cho một phòng.
     * Chuyển từng bản ghi liên kết thành RoomServiceResponse trước khi trả về.
     *
     * GET /be/rooms/{roomId}/services
     */
    @GetMapping("/be/rooms/{roomId}/services")
    public List<RoomServiceResponse> roomServices(
            @PathVariable UUID roomId
    ) {
        return service.listRoomServices(roomId)
                .stream()
                .map(RoomServiceResponse::from)
                .toList();
    }

    /**
     * Gán một dịch vụ vào phòng.
     * Trả về thông tin liên kết vừa tạo với HTTP 201 Created.
     *
     * POST /be/rooms/{roomId}/services/{serviceId}
     */
    @PostMapping("/be/rooms/{roomId}/services/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomServiceResponse assign(
            @PathVariable UUID roomId,
            @PathVariable UUID serviceId
    ) {
        return RoomServiceResponse.from(
                service.assign(roomId, serviceId)
        );
    }

    /**
     * Cập nhật trạng thái hoạt động của dịch vụ trong một phòng.
     * Dữ liệu trạng thái được nhận từ request body và kiểm tra bằng Bean Validation.
     *
     * PUT /be/rooms/{roomId}/services/{serviceId}
     */
    @PutMapping("/be/rooms/{roomId}/services/{serviceId}")
    public RoomServiceResponse update(
            @PathVariable UUID roomId,
            @PathVariable UUID serviceId,
            @Valid @RequestBody RoomServiceStatusRequest request
    ) {
        return RoomServiceResponse.from(
                service.update(roomId, serviceId, request)
        );
    }

    /**
     * Xóa liên kết giữa một phòng và một dịch vụ.
     * Không trực tiếp xóa phòng hoặc dịch vụ khỏi danh mục.
     * Trả về HTTP 204 No Content nếu thao tác thành công.
     *
     * DELETE /be/rooms/{roomId}/services/{serviceId}
     */
    @DeleteMapping("/be/rooms/{roomId}/services/{serviceId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID roomId,
            @PathVariable UUID serviceId
    ) {
        service.delete(roomId, serviceId);

        return ResponseEntity.noContent().build();
    }
}