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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.InvoiceLifecycleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_invoices_number", columnNames = "invoice_number"),
                @UniqueConstraint(
                        name = "uq_invoices_contract_period",
                        columnNames = {"contract_id", "period_start", "period_end"}
                )
        },
        indexes = {
                @Index(name = "ix_invoices_status_due", columnList = "lifecycle_status, due_date"),
                @Index(name = "ix_invoices_contract_issued", columnList = "contract_id, issued_at")
        }
)
public class Invoice {

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
    @JoinColumn(name = "contract_id", nullable = false)
    private RentalContract contract;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "issued_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime issuedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @PositiveOrZero
    @Column(
            name = "subtotal",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal subtotal = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(
            name = "total_amount",
            nullable = false,
            precision = 15,
            scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0"
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "lifecycle_status",
            nullable = false,
            columnDefinition = "ENUM('draft','issued','cancelled') DEFAULT 'draft'"
    )
    private InvoiceLifecycleStatus lifecycleStatus = InvoiceLifecycleStatus.DRAFT;

    @Lob
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;
}
