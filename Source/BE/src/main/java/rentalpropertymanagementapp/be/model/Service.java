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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.CalculationMethod;
import rentalpropertymanagementapp.be.model.enums.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "services",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_services_property_code",
                columnNames = {"property_id", "code"}
        ),
        indexes = @Index(
                name = "ix_services_property_type_active",
                columnList = "property_id, service_type, is_active"
        )
)
public class Service {

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
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "service_type",
            nullable = false,
            columnDefinition = "ENUM('electricity','water','wifi','parking','garbage','cleaning','other')"
    )
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "calculation_method",
            nullable = false,
            columnDefinition = "ENUM('metered','fixed','per_person','per_vehicle','per_unit')"
    )
    private CalculationMethod calculationMethod;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @PositiveOrZero
    @Column(
            name = "default_unit_price",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal defaultUnitPrice = BigDecimal.ZERO;

    @Column(name = "is_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean required = false;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;
}
