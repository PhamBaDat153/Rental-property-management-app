package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Room.Room;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    // Kiểm tra địa điểm đã có phòng nào hay chưa.
    @Query("""
            select count(r) > 0
            from Room r
            where r.location.location_id = :locationId
            """)
    boolean existsByLocationId(
            @Param("locationId") UUID locationId
    );

    // Kiểm tra mã phòng đã tồn tại trong một địa điểm hay chưa.
    @Query("""
            select count(r) > 0
            from Room r
            where r.location.location_id = :locationId
              and r.room_code = :roomCode
            """)
    boolean existsByLocationIdAndRoomCode(
            @Param("locationId") UUID locationId,
            @Param("roomCode") String roomCode
    );

    // Kiểm tra mã phòng có bị trùng với phòng khác trong cùng địa điểm hay không.
    // Loại trừ phòng có UUID được truyền vào, thường dùng khi cập nhật phòng.
    @Query("""
            select count(r) > 0
            from Room r
            where r.location.location_id = :locationId
              and r.room_code = :roomCode
              and r.room_id <> :roomId
            """)
    boolean existsByLocationIdAndRoomCodeAndRoomIdNot(
            @Param("locationId") UUID locationId,
            @Param("roomCode") String roomCode,
            @Param("roomId") UUID roomId
    );
}