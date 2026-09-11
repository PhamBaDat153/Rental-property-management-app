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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "announcement_recipients",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_recipients_announcement_tenant",
                columnNames = {"announcement_id", "tenant_id"}
        ),
        indexes = @Index(name = "ix_recipients_user_read", columnList = "user_id, read_at")
)
public class AnnouncementRecipient {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "delivery_status",
            nullable = false,
            columnDefinition = "ENUM('pending','sent','delivered','failed') DEFAULT 'pending'"
    )
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "sent_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime readAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}
