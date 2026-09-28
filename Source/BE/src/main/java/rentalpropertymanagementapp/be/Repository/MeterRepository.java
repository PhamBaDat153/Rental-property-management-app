package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Meter.Meter;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    @Query("select meter from Meter meter where meter.room.room_id = :roomId")
    List<Meter> findByRoomId(@Param("roomId") UUID roomId);
}
