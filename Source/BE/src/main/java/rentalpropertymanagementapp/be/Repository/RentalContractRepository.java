package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;

import java.util.UUID;

public interface RentalContractRepository extends JpaRepository<RentalContract, UUID> {
    @Query("select count(c) > 0 from RentalContract c where c.room.room_id = :roomId")
    boolean existsByRoomId(@Param("roomId") UUID roomId);
}
