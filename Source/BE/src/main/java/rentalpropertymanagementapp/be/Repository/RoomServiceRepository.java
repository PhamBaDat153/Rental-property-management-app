package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Model.Service.RoomServiceId;

import java.util.List;
import java.util.UUID;

public interface RoomServiceRepository
        extends JpaRepository<RoomService, RoomServiceId> {

    // Lấy danh sách dịch vụ được gán cho một phòng theo UUID phòng.
    @Query("""
            select value
            from RoomService value
            where value.room.room_id = :roomId
            """)
    List<RoomService> findByRoomId(
            @Param("roomId") UUID roomId
    );

    // Kiểm tra dịch vụ đã được gán cho phòng hay chưa.
    // Trả về true nếu tồn tại liên kết giữa phòng và dịch vụ.
    @Query("""
            select count(value) > 0
            from RoomService value
            where value.id.room_id = :roomId
              and value.id.service_id = :serviceId
            """)
    boolean existsAssignment(
            @Param("roomId") UUID roomId,
            @Param("serviceId") UUID serviceId
    );
}