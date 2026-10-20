package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.AnnouncementRequest;
import rentalpropertymanagementapp.be.DTO.AnnouncementResponse;
import rentalpropertymanagementapp.be.Model.Announcement.Announcement;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {

    // Lấy danh sách tất cả thông báo.
    List<Announcement> list();

    // Lấy thông tin thông báo theo UUID.
    Announcement get(UUID id);

    // Tạo mới hoặc cập nhật thông báo và danh sách người nhận.
    Announcement save(UUID id, AnnouncementRequest request);

    // Xóa thông báo theo UUID.
    void delete(UUID id);

    // Chuyển entity thông báo thành DTO phản hồi.
    AnnouncementResponse response(Announcement value);
}