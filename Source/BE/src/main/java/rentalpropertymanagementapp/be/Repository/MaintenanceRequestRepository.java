package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository
        extends JpaRepository<MaintenanceRequest, UUID> {

    // Tìm kiếm yêu cầu bảo trì theo phòng, trạng thái và mức độ ưu tiên.
    // Nếu một tham số lọc là null, bỏ qua điều kiện lọc tương ứng.
    // Sắp xếp theo thời gian tạo giảm dần, sau đó theo UUID giảm dần.
    @Query("""
            select r
            from MaintenanceRequest r
            where (:roomId is null
                   or r.room.room_id = :roomId)
              and (:status is null
                   or r.status = :status)
              and (:priority is null
                   or r.priority = :priority)
            order by r.created_at desc, r.request_id desc
            """)
    List<MaintenanceRequest> search(
            @Param("roomId") UUID roomId,
            @Param("status") MaintenanceStatus status,
            @Param("priority") String priority
    );
}