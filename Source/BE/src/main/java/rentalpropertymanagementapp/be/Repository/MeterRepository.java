package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Meter.Meter;

import java.util.List;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {

    // Lấy danh sách công tơ thuộc một phòng theo UUID của phòng.
    @Query("""
            select meter
            from Meter meter
            where meter.room.room_id = :roomId
            """)
    List<Meter> findByRoomId(
            @Param("roomId") UUID roomId
    );
}