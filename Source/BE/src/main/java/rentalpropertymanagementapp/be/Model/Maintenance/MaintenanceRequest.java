package rentalpropertymanagementapp.be.Model.Maintenance;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.User.User;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "maintenance_requests",
        indexes = {
                @Index(name = "idx_maintenance_user_id", columnList = "user_id"),
                @Index(name = "idx_maintenance_room_id", columnList = "room_id"),
                @Index(name = "idx_maintenance_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest {
    @Id
    @Column(name = "request_id", nullable = false)
    private UUID request_id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, length = 30)
    private String priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;
    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime completed_at;
    @OneToMany(
            mappedBy = "request",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<RequestImage> images = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        request_id = Generators.timeBasedEpochGenerator().generate();
        created_at = LocalDateTime.now();
        if (priority == null) priority = "NORMAL";
        if (status == null) status = MaintenanceStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
