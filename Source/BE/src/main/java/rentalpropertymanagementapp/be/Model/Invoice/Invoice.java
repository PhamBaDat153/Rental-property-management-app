package rentalpropertymanagementapp.be.Model.Invoice;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "invoice",
        uniqueConstraints = @UniqueConstraint(name = "uq_invoice_number", columnNames = "invoice_number"),
        indexes = {
                @Index(name = "idx_invoice_contract_id", columnList = "contract_id"),
                @Index(name = "idx_invoice_due_date", columnList = "due_date"),
                @Index(name = "idx_invoice_issued_at", columnList = "issued_at"),
                @Index(name = "idx_invoice_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @Column(name = "invoice_id", nullable = false)
    private UUID invoice_id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "contract_id", nullable = false)
    private RentalContract contract;

    @Column(nullable = false, length = 100)
    private String invoice_number;

    @Column(nullable = false)
    private LocalDateTime issued_at;
    private LocalDate due_date;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal discount_amount;
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal tax_amount;
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal total_amount;
    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "invoice", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, orphanRemoval = true)
    private Set<InvoiceItem> items = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        invoice_id = Generators.timeBasedEpochGenerator().generate();
        created_at = LocalDateTime.now();
        issued_at = issued_at == null ? created_at : issued_at;
        if (status == null) status = InvoiceStatus.SENTED;
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (discount_amount == null) discount_amount = BigDecimal.ZERO;
        if (tax_amount == null) tax_amount = BigDecimal.ZERO;
        if (total_amount == null) total_amount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
