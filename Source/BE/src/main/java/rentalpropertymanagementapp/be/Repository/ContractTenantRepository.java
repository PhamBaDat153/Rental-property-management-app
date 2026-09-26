package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenantId;

import java.util.UUID;

public interface ContractTenantRepository extends JpaRepository<ContractTenant, ContractTenantId> {
    @Query("select count(ct) > 0 from ContractTenant ct where ct.id.contract_id = :contractId")
    boolean existsByContractId(@Param("contractId") UUID contractId);
    @Query("select count(ct) > 0 from ContractTenant ct where ct.tenant.tenant_id = :tenantId")
    boolean existsByTenantId(@Param("tenantId") UUID tenantId);
}
