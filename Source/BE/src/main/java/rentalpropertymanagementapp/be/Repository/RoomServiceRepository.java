package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Model.Service.RoomServiceId;

public interface RoomServiceRepository extends JpaRepository<RoomService, RoomServiceId> {
    @Query("select value from RoomService value where value.room.room_id = :roomId")
    List<RoomService> findByRoomId(@Param("roomId") UUID roomId);
    @Query("select count(value) > 0 from RoomService value where value.id.room_id = :roomId and value.id.service_id = :serviceId")
    boolean existsAssignment(@Param("roomId") UUID roomId, @Param("serviceId") UUID serviceId);
}
