package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.Room.Location;

import java.util.UUID;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Query("select l from Location l where l.location_code = :code")
    Optional<Location> findByCode(@Param("code") String code);
    @Query("select count(l) > 0 from Location l where l.location_code = :code")
    boolean existsByCode(@Param("code") String code);
    @Query("select count(l) > 0 from Location l where l.location_code = :code and l.location_id <> :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
}
