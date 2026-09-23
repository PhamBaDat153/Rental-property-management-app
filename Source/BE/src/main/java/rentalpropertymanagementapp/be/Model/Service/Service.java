package rentalpropertymanagementapp.be.Model.Service;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "service",
        uniqueConstraints = @UniqueConstraint(name = "uq_service_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    @Id
    @Column(name = "service_id", nullable = false)
    private UUID service_id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false, length = 50)
    private String calculation_method;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal default_unit_price;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private AvailableStatus room_status;

    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(
            mappedBy = "service",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}
    )
    private Set<RoomService> roomServices = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        service_id = Generators.timeBasedEpochGenerator().generate();
        created_at = LocalDateTime.now();
        if (room_status == null) room_status = AvailableStatus.AVAILABLE;
        if (default_unit_price == null) default_unit_price = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
