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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "rental_contracts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_contracts_number",
                columnNames = "contract_number"
        ),
        indexes = {
                @Index(
                        name = "ix_contracts_room_dates",
                        columnList = "room_id, status, start_date, end_date"
                ),
                @Index(name = "ix_contracts_status_end", columnList = "status, end_date")
        }
)
public class RentalContract {

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

    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "signed_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime signedAt;

    @PositiveOrZero
    @Column(name = "rent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal rentAmount;

    @PositiveOrZero
    @Column(
            name = "deposit_required",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal depositRequired = BigDecimal.ZERO;

    @Min(1)
    @Max(31)
    @Column(name = "billing_day", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private short billingDay;

    @Column(
            name = "payment_due_days",
            nullable = false,
            columnDefinition = "TINYINT UNSIGNED DEFAULT 0"
    )
    private short paymentDueDays = 0;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "ENUM('draft','active','expired','terminated','cancelled') DEFAULT 'draft'"
    )
    private ContractStatus status = ContractStatus.DRAFT;

    @Lob
    @Column(name = "terms", columnDefinition = "LONGTEXT")
    private String terms;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "terminated_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime terminatedAt;

    @Column(name = "termination_reason", length = 1000)
    private String terminationReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;
}
