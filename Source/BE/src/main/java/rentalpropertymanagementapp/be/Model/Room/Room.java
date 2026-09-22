package rentalpropertymanagementapp.be.Model.Room;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.AvalibleStatus;
import rentalpropertymanagementapp.be.Model.Property;
import rentalpropertymanagementapp.be.Model.Tenant.Tenant;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "room",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_room_property_code",
                        columnNames = {"property_id", "room_code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_room_property_id",
                        columnList = "property_id"
                ),
                @Index(
                        name = "idx_room_status",
                        columnList = "room_status"
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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "property_property_id")
    private Property property;

    @Column(name = "room_code", nullable = false, unique = true, length = 50)
    private String room_code;

    @Column(name = "room_name", nullable = false, length = 100)
    private String room_name;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "area_m_2", nullable = false)
    private Double area_m2;

    @Column(name = "max_occupants", nullable = false)
    private Integer max_occupants;

    @Column(name = "rent_price", nullable = false)
    private Double rent_price;

    @Enumerated
    @Column(name = "room_status", nullable = false)
    private AvalibleStatus room_status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomImage> roomImages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tenant> tenants = new LinkedHashSet<>();


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        room_status = AvalibleStatus.AVAILABLE;
        room_id = Generators.timeBasedEpochGenerator().generate();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
