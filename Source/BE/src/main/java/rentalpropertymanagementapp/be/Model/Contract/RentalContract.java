package rentalpropertymanagementapp.be.Model.Contract;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "rental_contracts",
        indexes = {
                @Index(
                        name = "idx_contract_room_id",
                        columnList = "room_id"
                ),
                @Index(
                        name = "idx_contract_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_contract_dates",
                        columnList = "start_date, end_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @Column(name = "contract_id", nullable = false)
    private UUID contract_id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_date", nullable = false)
    private LocalDate start_date;

    @Column(name = "end_date")
    private LocalDate end_date;

    @Column(name = "signed_at")
    private LocalDateTime signed_at;

    @Column(name = "rent_amount", nullable = false)
    private BigDecimal rent_amount;

    @Column(name = "deposit_required", nullable = false)
    private BigDecimal deposit_required;

    @Column(name = "billing_day")
    private Integer billing_day;

    @Column(name = "payment_due_days", nullable = false)
    private Integer payment_due_days;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActiveStatus status;

    @Column(name = "terms")
    private String terms;

    @Column(name = "document_url")
    private String document_url;

    @Column(name = "terminated_at")
    private LocalDateTime terminated_at;

    @Column(name = "termination_reason")
    private String termination_reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @OneToMany(
            mappedBy = "rentalContract",
            cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<ContractTenant> contractTenants = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "contract",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true
    )
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
        this.contract_id = Generators.timeBasedEpochGenerator().generate();
        this.status = ActiveStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }

}
