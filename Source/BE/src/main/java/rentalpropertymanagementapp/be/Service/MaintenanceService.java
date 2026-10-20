package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.MaintenanceResponse;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    // Lấy danh sách yêu cầu bảo trì theo phòng, trạng thái và mức độ ưu tiên.
    List<MaintenanceRequest> list(
            UUID roomId,
            MaintenanceStatus status,
            String priority
    );

    // Lấy chi tiết yêu cầu bảo trì theo UUID.
    MaintenanceRequest get(UUID id);

    // Tạo mới hoặc cập nhật yêu cầu bảo trì.
    MaintenanceRequest save(
            UUID id,
            rentalpropertymanagementapp.be.DTO.MaintenanceRequest request
    );

    // Xóa yêu cầu bảo trì theo UUID.
    void delete(UUID id);

    // Chuyển entity yêu cầu bảo trì thành DTO để trả về cho API.
    MaintenanceResponse response(MaintenanceRequest value);
}