package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.Room.Room;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Query("select count(r) > 0 from Room r where r.location.location_id = :locationId")
    boolean existsByLocationId(@Param("locationId") UUID locationId);
    @Query("select count(r) > 0 from Room r where r.location.location_id = :locationId and r.room_code = :roomCode")
    boolean existsByLocationIdAndRoomCode(@Param("locationId") UUID locationId, @Param("roomCode") String roomCode);
    @Query("select count(r) > 0 from Room r where r.location.location_id = :locationId and r.room_code = :roomCode and r.room_id <> :roomId")
    boolean existsByLocationIdAndRoomCodeAndRoomIdNot(@Param("locationId") UUID locationId, @Param("roomCode") String roomCode, @Param("roomId") UUID roomId);
}
