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

import rentalpropertymanagementapp.be.DTO.LocationRequest;
import rentalpropertymanagementapp.be.DTO.LocationResponse;
import rentalpropertymanagementapp.be.Model.Room.Location;
import rentalpropertymanagementapp.be.Service.LocationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/locations")
public class LocationController {

    private final LocationService locationService;

    // Khởi tạo Controller và tiêm service quản lý địa điểm.
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // -------------------------------------------------------------------------
    // Danh sách địa điểm
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả địa điểm trong hệ thống.
     * Chuyển từng entity Location thành LocationResponse trước khi trả về.
     *
     * GET /be/locations
     */
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations() {
        List<LocationResponse> locations = locationService.getLocations()
                .stream()
                .map(LocationResponse::from)
                .toList();

        return ResponseEntity.ok(locations);
    }

    // -------------------------------------------------------------------------
    // Chi tiết địa điểm theo UUID
    // -------------------------------------------------------------------------

    /**
     * Lấy thông tin địa điểm theo UUID.
     * Trả về HTTP 404 Not Found nếu không tìm thấy địa điểm.
     *
     * GET /be/locations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(
            @PathVariable UUID id
    ) {
        Location location = locationService.getLocationById(id);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                LocationResponse.from(location)
        );
    }

    // -------------------------------------------------------------------------
    // Chi tiết địa điểm theo mã
    // -------------------------------------------------------------------------

    /**
     * Tìm địa điểm theo mã địa điểm.
     * Trả về HTTP 404 Not Found nếu không tìm thấy địa điểm tương ứng.
     *
     * GET /be/locations/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<LocationResponse> getLocationByCode(
            @PathVariable String code
    ) {
        Location location = locationService.getLocationByCode(code);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                LocationResponse.from(location)
        );
    }

    // -------------------------------------------------------------------------
    // Tạo địa điểm
    // -------------------------------------------------------------------------

    /**
     * Tạo địa điểm mới từ dữ liệu request.
     * Kiểm tra dữ liệu đầu vào bằng Bean Validation.
     * Trả về HTTP 201 Created cùng thông tin địa điểm vừa tạo.
     *
     * POST /be/locations
     */
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody LocationRequest request
    ) {
        Location location = locationService.createLocation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(LocationResponse.from(location));
    }

    // -------------------------------------------------------------------------
    // Cập nhật địa điểm
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin địa điểm theo UUID.
     * Kiểm tra dữ liệu request bằng Bean Validation.
     * Trả về HTTP 404 nếu service không tìm thấy địa điểm cần cập nhật;
     * nếu thành công, trả về HTTP 200 OK cùng thông tin mới.
     *
     * PUT /be/locations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody LocationRequest request
    ) {
        Location location = locationService.updateLocation(request, id);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                LocationResponse.from(location)
        );
    }

    // -------------------------------------------------------------------------
    // Xóa địa điểm
    // -------------------------------------------------------------------------

    /**
     * Xóa địa điểm theo UUID.
     * Kiểm tra địa điểm tồn tại trước khi thực hiện xóa.
     * Trả về HTTP 404 nếu không tìm thấy, hoặc HTTP 204 No Content
     * nếu xóa thành công.
     *
     * DELETE /be/locations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable UUID id
    ) {
        if (locationService.getLocationById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        locationService.deleteLocation(id);

        return ResponseEntity.noContent().build();
    }
}