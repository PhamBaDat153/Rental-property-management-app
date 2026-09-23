package rentalpropertymanagementapp.be.Model.Contract;

import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Model.User.Tenant;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_tenants", indexes = {
        @Index(name = "idx_contract_tenants_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_contract_tenants_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTenant {

    @EmbeddedId
    private ContractTenantId id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("contract_id")
    @JoinColumn(name = "contract_id")
    private RentalContract rentalContract;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("tenant_id")
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "is_representative", nullable = false)
    private Boolean is_representative;

    private LocalDate move_in_date;
    private LocalDate move_out_date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status;

    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @PrePersist
    public void prePersist() {
        created_at = LocalDateTime.now();
        if (status == null) status = ActiveStatus.ACTIVE;
        if (is_representative == null) is_representative = false;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
