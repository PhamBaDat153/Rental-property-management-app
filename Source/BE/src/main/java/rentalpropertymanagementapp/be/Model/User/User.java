package rentalpropertymanagementapp.be.Model.User;

import com.fasterxml.uuid.Generators;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import rentalpropertymanagementapp.be.Model.Announcement.UserAnnouncement;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_tenant_id",
                        columnNames = "tenant_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_role_id",
                        columnList = "role_id"
                ),
                @Index(
                        name = "idx_user_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID user_id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String user_name;

    @Column(name = "password_hash")
    @JsonIgnore
    private String password_hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActiveStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}
    )
    private Set<UserAnnouncement> userAnnouncements = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}
    )
    private Set<MaintenanceRequest> maintenanceRequests = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
        this.user_id = Generators.timeBasedEpochGenerator().generate();
        if (status == null) status = ActiveStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }


}
