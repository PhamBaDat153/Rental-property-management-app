package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.MaintenanceResponse;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;
import rentalpropertymanagementapp.be.Repository.MaintenanceRequestRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MaintenanceServiceImplement {

    private final MaintenanceRequestRepository requests;
    private final UserRepository users;
    private final RoomRepository rooms;

    /**
     * Khởi tạo service và tiêm các Repository cần thiết.
     * Các Repository dùng để truy vấn yêu cầu bảo trì, người dùng và phòng.
     */
    public MaintenanceServiceImplement(
            MaintenanceRequestRepository requests,
            UserRepository users,
            RoomRepository rooms
    ) {
        this.requests = requests;
        this.users = users;
        this.rooms = rooms;
    }

    /**
     * Lấy danh sách yêu cầu bảo trì theo phòng, trạng thái và mức độ ưu tiên.
     * Các tham số lọc được chuyển trực tiếp đến Repository để truy vấn dữ liệu.
     */
    public List<MaintenanceRequest> list(
            UUID roomId,
            MaintenanceStatus status,
            String priority
    ) {
        return requests.search(roomId, status, priority);
    }

    /**
     * Tìm yêu cầu bảo trì theo UUID.
     * Nếu không tìm thấy yêu cầu, ném ResourceNotFoundException.
     */
    public MaintenanceRequest get(UUID id) {
        return requests.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Maintenance request not found"
                        )
                );
    }

    /**
     * Tạo mới hoặc cập nhật yêu cầu bảo trì.
     * Nếu id null thì tạo yêu cầu mới; ngược lại lấy yêu cầu hiện có.
     * Kiểm tra người dùng và phòng tồn tại, sau đó cập nhật tiêu đề,
     * mô tả, mức độ ưu tiên và trạng thái.
     * Nếu trạng thái là COMPLETED, ghi nhận thời điểm hoàn thành
     * nếu chưa có thời điểm này. Nếu trạng thái khác, xóa thời điểm hoàn thành.
     * Cuối cùng lưu yêu cầu vào cơ sở dữ liệu.
     */
    public MaintenanceRequest save(
            UUID id,
            rentalpropertymanagementapp.be.DTO.MaintenanceRequest request
    ) {
        MaintenanceRequest value = id == null
                ? new MaintenanceRequest()
                : get(id);

        value.setUser(
                users.findById(request.user_id())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        )
        );

        value.setRoom(
                rooms.findById(request.room_id())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Room not found"
                                )
                        )
        );

        value.setTitle(request.title().trim());
        value.setDescription(request.description());
        value.setPriority(
                request.priority().trim().toUpperCase(Locale.ROOT)
        );
        value.setStatus(request.status());

        value.setCompleted_at(
                request.status() == MaintenanceStatus.COMPLETED
                        ? (value.getCompleted_at() == null
                        ? LocalDateTime.now()
                        : value.getCompleted_at())
                        : null
        );

        return requests.save(value);
    }

    /**
     * Xóa yêu cầu bảo trì theo UUID.
     * Trước khi xóa, lấy yêu cầu hiện có để xác nhận bản ghi tồn tại.
     * Thao tác xóa được thực hiện trong một transaction.
     */
    @Transactional
    public void delete(UUID id) {
        requests.delete(get(id));
    }

    /**
     * Chuyển đối tượng yêu cầu bảo trì thành MaintenanceResponse.
     * Dữ liệu trả về bao gồm thông tin người dùng, phòng, nội dung yêu cầu,
     * trạng thái, thời gian tạo/cập nhật, thời điểm hoàn thành
     * và danh sách URL ảnh đính kèm.
     */
    public MaintenanceResponse response(MaintenanceRequest value) {
        return new MaintenanceResponse(
                value.getRequest_id(),
                value.getUser().getUser_id(),
                value.getRoom().getRoom_id(),
                value.getTitle(),
                value.getDescription(),
                value.getPriority(),
                value.getStatus(),
                value.getCreated_at(),
                value.getUpdated_at(),
                value.getCompleted_at(),
                value.getImages()
                        .stream()
                        .map(image -> image.getFile_url())
                        .toList()
        );
    }
}