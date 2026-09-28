package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Meter.MeterReading;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    @Query("select r from MeterReading r where r.meter.meter_id = :meterId order by r.reading_at desc, r.reading_id desc")
    List<MeterReading> findByMeterId(@Param("meterId") UUID meterId);
}
