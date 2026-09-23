package rentalpropertymanagementapp.be.Model.Meter;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meter_reading", indexes = {
        @Index(name = "idx_meter_reading_meter_id", columnList = "meter_id"),
        @Index(name = "idx_meter_reading_meter_date", columnList = "meter_id, reading_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {
    @Id @Column(name = "reading_id", nullable = false)
    private UUID reading_id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;
    @Column(nullable = false) private LocalDateTime reading_at;
    @Column(nullable = false, precision = 15, scale = 3) private BigDecimal current_value;
    @Column(nullable = false, precision = 15, scale = 3) private BigDecimal previous_value;
    @Column(nullable = false, precision = 15, scale = 3) private BigDecimal quantity;
    @Column(columnDefinition = "TEXT") private String evidence_url;
    @Column(length = 500) private String note;
    @Column(nullable = false) private LocalDateTime created_at;
    private LocalDateTime updated_at;
    @PrePersist public void prePersist() { reading_id = Generators.timeBasedEpochGenerator().generate(); created_at = LocalDateTime.now(); }
    @PreUpdate public void preUpdate() { updated_at = LocalDateTime.now(); }
}
