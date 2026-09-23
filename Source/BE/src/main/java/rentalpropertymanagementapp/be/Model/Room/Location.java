package rentalpropertymanagementapp.be.Model.Room;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.AnnouncementStatus;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "location",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_location_code",
                        columnNames = "location_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_location_address",
                        columnList = "province_name, district_name, ward_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @Column(name = "location_id", nullable = false)
    private UUID location_id;

    @Column(name = "location_code", nullable = false, length = 50)
    private String location_code;

    @Column(name = "address_line", nullable = false)
    private String address_line;

    @Column(name = "ward_name")
    private String ward_name;

    @Column(name = "district_name")
    private String district_name;

    @Column(name = "province_name", nullable = false)
    private String province_name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AvailableStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @OneToMany(
            mappedBy = "location",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<Room> rooms = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        location_id = Generators.timeBasedEpochGenerator().generate();
        created_at = LocalDateTime.now();
        if (status == null) status = AvailableStatus.AVAILABLE;

    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }
}
