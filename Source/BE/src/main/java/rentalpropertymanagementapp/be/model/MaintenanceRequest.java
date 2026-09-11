package rentalpropertymanagementapp.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.MaintenancePriority;
import rentalpropertymanagementapp.be.model.enums.MaintenanceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "maintenance_requests",
        indexes = {
                @Index(
                        name = "ix_maintenance_room_status_time",
                        columnList = "room_id, status, submitted_at"
                ),
                @Index(
                        name = "ix_maintenance_queue",
                        columnList = "status, priority, submitted_at"
                )
        }
)
public class MaintenanceRequest {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private RentalContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_tenant_id")
    private Tenant reportedByTenant;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            columnDefinition = "ENUM('low','normal','high','urgent') DEFAULT 'normal'"
    )
    private MaintenancePriority priority = MaintenancePriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "ENUM('submitted','accepted','in_progress','waiting','completed','cancelled','rejected') DEFAULT 'submitted'"
    )
    private MaintenanceStatus status = MaintenanceStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @Column(
            name = "submitted_at",
            nullable = false,
            columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)"
    )
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "completed_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime completedAt;

    @Column(name = "tenant_confirmed_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime tenantConfirmedAt;

    @Column(name = "cancelled_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime cancelledAt;
}
