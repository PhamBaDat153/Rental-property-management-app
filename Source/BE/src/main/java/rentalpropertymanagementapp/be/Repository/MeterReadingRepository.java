package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Meter.MeterReading;

import java.util.List;
import java.util.UUID;

public interface MeterReadingRepository
        extends JpaRepository<MeterReading, UUID> {

    // Lấy danh sách các lần ghi chỉ số của một công tơ.
    // Sắp xếp theo thời gian ghi chỉ số giảm dần,
    // sau đó theo UUID chỉ số giảm dần.
    @Query("""
            select r
            from MeterReading r
            where r.meter.meter_id = :meterId
            order by r.reading_at desc, r.reading_id desc
            """)
    List<MeterReading> findByMeterId(
            @Param("meterId") UUID meterId
    );

}
