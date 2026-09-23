package rentalpropertymanagementapp.be.Model.Meter;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Room.Room;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meter", indexes = {
        @Index(name = "idx_meter_room_id", columnList = "room_id"),
        @Index(name = "idx_meter_room_type", columnList = "room_id, meter_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meter {
    @Id
    @Column(name = "meter_id", nullable = false)
    private UUID meter_id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Column(nullable = false, length = 50)
    private String meter_type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailableStatus status;
    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    @OneToMany(mappedBy = "meter", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, orphanRemoval = true)
    private Set<MeterReading> readings = new LinkedHashSet<>();
    @PrePersist public void prePersist() { meter_id = Generators.timeBasedEpochGenerator().generate(); created_at = LocalDateTime.now(); if (status == null) status = AvailableStatus.AVAILABLE; }
    @PreUpdate public void preUpdate() { updated_at = LocalDateTime.now(); }
}
