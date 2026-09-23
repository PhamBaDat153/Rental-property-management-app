package rentalpropertymanagementapp.be.Model.Room;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Model.Meter.Meter;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;

@Entity
@Table(
        name = "room",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_room_location_code",
                        columnNames = {"location_id", "room_code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_room_location_id",
                        columnList = "location_id"
                ),
                @Index(
                        name = "idx_room_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @Column(name = "room_id", nullable = false)
    private UUID room_id;

    @Column(name = "room_code", nullable = false, length = 50)
    private String room_code;

    @Column(name = "room_name", length = 100)
    private String room_name;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal area_m2;

    @Column(name = "max_occupants", nullable = false)
    private Integer max_occupants;

    @Column(name = "rent_price", nullable = false, precision = 15, scale = 0)
    private BigDecimal rent_price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AvailableStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(
            mappedBy = "room",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<RentalContract> rentalContracts = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "room",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<RoomService> roomServices = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "room",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<RoomImage> images = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "room",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<Meter> meters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<MaintenanceRequest> maintenanceRequests = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        this.room_id = Generators.timeBasedEpochGenerator().generate();
        if (status == null) this.status = AvailableStatus.AVAILABLE;
        this.created_at = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }
}
