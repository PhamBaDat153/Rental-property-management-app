package rentalpropertymanagementapp.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tenant_invitations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_invitations_token_hash",
                columnNames = "token_hash"
        ),
        indexes = {
                @Index(
                        name = "ix_invitations_tenant_state",
                        columnList = "tenant_id, used_at, revoked_at"
                ),
                @Index(name = "ix_invitations_expiry", columnList = "expires_at")
        }
)
public class TenantInvitation {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "invited_phone", nullable = false, length = 20)
    private String invitedPhone;

    @Column(name = "token_hash", nullable = false, length = 32, columnDefinition = "BINARY(32)")
    private byte[] tokenHash;

    @Column(name = "expires_at", nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime expiresAt;

    @Column(name = "used_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id")
    private User usedByUser;

    @Column(name = "revoked_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;
}
