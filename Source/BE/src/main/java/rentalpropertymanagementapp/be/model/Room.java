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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.RoomOperationalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "rooms",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_rooms_property_code",
                columnNames = {"property_id", "code"}
        ),
        indexes = @Index(
                name = "ix_rooms_property_status",
                columnList = "property_id, operational_status"
        )
)
public class Room {

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

    @Column(name = "deleted_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "floor")
    private Short floor;

    @Column(name = "area_m2", precision = 8, scale = 2)
    private BigDecimal areaM2;

    @Positive
    @Column(name = "max_occupants", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int maxOccupants;

    @PositiveOrZero
    @Column(
            name = "suggested_rent",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal suggestedRent = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(
            name = "suggested_deposit",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal suggestedDeposit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "operational_status",
            nullable = false,
            columnDefinition = "ENUM('available','maintenance','inactive') DEFAULT 'available'"
    )
    private RoomOperationalStatus operationalStatus = RoomOperationalStatus.AVAILABLE;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
