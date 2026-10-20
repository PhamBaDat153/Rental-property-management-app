package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.RoomServiceStatusRequest;
import rentalpropertymanagementapp.be.DTO.ServiceRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Model.Service.RoomServiceId;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Repository.RoomServiceRepository;
import rentalpropertymanagementapp.be.Repository.ServiceRepository;

import java.util.List;
import java.util.UUID;

@Service
public class RoomUtilityServiceImplement implements RoomUtilityService {

    private final RoomRepository rooms;
    private final ServiceRepository services;
    private final RoomServiceRepository roomServices;

    /**
     * Khởi tạo service và tiêm các Repository cần thiết.
     * RoomRepository quản lý phòng, ServiceRepository quản lý danh mục dịch vụ,
     * RoomServiceRepository quản lý dịch vụ được gán cho từng phòng.
     */
    public RoomUtilityServiceImplement(
            RoomRepository rooms,
            ServiceRepository services,
            RoomServiceRepository roomServices
    ) {
        this.rooms = rooms;
        this.services = services;
        this.roomServices = roomServices;
    }

    /**
     * Lấy toàn bộ danh mục dịch vụ hiện có trong hệ thống.
     */
    public List<rentalpropertymanagementapp.be.Model.Service.Service> listServices() {
        return services.findAll();
    }

    /**
     * Tạo dịch vụ mới trong danh mục.
     * Chuẩn hóa khoảng trắng ở đầu và cuối tên dịch vụ, đơn vị tính
     * và phương thức tính trước khi lưu vào cơ sở dữ liệu.
     */
    public rentalpropertymanagementapp.be.Model.Service.Service createService(
            ServiceRequest request
    ) {
        return services.save(
                rentalpropertymanagementapp.be.Model.Service.Service.builder()
                        .name(request.name().trim())
                        .unit(request.unit().trim())
                        .calculation_method(
                                request.calculation_method().trim()
                        )
                        .default_unit_price(request.default_unit_price())
                        .room_status(request.room_status())
                        .build()
        );
    }

    /**
     * Cập nhật thông tin một dịch vụ theo UUID.
     * Nếu dịch vụ không tồn tại, ném ResourceNotFoundException.
     * Nếu tìm thấy, cập nhật các trường theo request rồi lưu lại.
     */
    public rentalpropertymanagementapp.be.Model.Service.Service updateService(
            UUID id,
            ServiceRequest request
    ) {
        var service = services.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Service not found"
                        )
                );

        service.setName(request.name().trim());
        service.setUnit(request.unit().trim());
        service.setCalculation_method(
                request.calculation_method().trim()
        );
        service.setDefault_unit_price(
                request.default_unit_price()
        );
        service.setRoom_status(request.room_status());

        return services.save(service);
    }

    /**
     * Xóa một dịch vụ khỏi danh mục theo UUID.
     * Nếu không tìm thấy dịch vụ, ném ResourceNotFoundException.
     * Thao tác được thực hiện trong transaction.
     */
    @Transactional
    public void deleteService(UUID id) {
        var service = services.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Service not found"
                        )
                );

        services.delete(service);
    }

    /**
     * Lấy danh sách dịch vụ được gán cho một phòng.
     * Kiểm tra phòng tồn tại trước khi truy vấn các bản ghi liên kết.
     */
    public List<RoomService> listRoomServices(UUID roomId) {
        room(roomId);

        return roomServices.findByRoomId(roomId);
    }

    /**
     * Gán một dịch vụ vào một phòng.
     * Kiểm tra phòng và dịch vụ tồn tại, đồng thời ngăn gán trùng
     * cùng một dịch vụ cho cùng một phòng.
     * Bản ghi liên kết mới được tạo với trạng thái is_active = true.
     */
    public RoomService assign(UUID roomId, UUID serviceId) {
        Room room = room(roomId);

        var service = services.findById(serviceId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Service not found"
                        )
                );

        if (roomServices.existsAssignment(roomId, serviceId)) {
            throw new ResourceConflictException(
                    "Service already assigned"
            );
        }

        return roomServices.save(
                RoomService.builder()
                        .id(new RoomServiceId(roomId, serviceId))
                        .room(room)
                        .service(service)
                        .is_active(true)
                        .build()
        );
    }

    /**
     * Cập nhật trạng thái hoạt động của một dịch vụ trong phòng.
     * Tìm bản ghi liên kết theo roomId và serviceId,
     * cập nhật trường is_active theo request rồi lưu lại.
     */
    public RoomService update(
            UUID roomId,
            UUID serviceId,
            RoomServiceStatusRequest request
    ) {
        RoomService roomService = get(roomId, serviceId);

        roomService.setIs_active(request.is_active());

        return roomServices.save(roomService);
    }

    /**
     * Xóa liên kết giữa một phòng và một dịch vụ.
     * Chỉ xóa bản ghi liên kết RoomService, không trực tiếp xóa
     * phòng hoặc dịch vụ trong danh mục.
     * Thao tác được thực hiện trong transaction.
     */
    @Transactional
    public void delete(UUID roomId, UUID serviceId) {
        roomServices.delete(get(roomId, serviceId));
    }

    /**
     * Tìm bản ghi liên kết giữa phòng và dịch vụ theo khóa tổng hợp.
     * Nếu liên kết không tồn tại, ném ResourceNotFoundException.
     */
    private RoomService get(UUID roomId, UUID serviceId) {
        RoomServiceId id = new RoomServiceId(roomId, serviceId);

        return roomServices.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Room service not found"
                        )
                );
    }

    /**
     * Tìm phòng theo UUID.
     * Nếu phòng không tồn tại, ném ResourceNotFoundException.
     */
    private Room room(UUID id) {
        return rooms.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Room not found"
                        )
                );
    }
}