package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenantId;

import java.util.List;
import java.util.UUID;

public interface ContractTenantRepository
        extends JpaRepository<ContractTenant, ContractTenantId> {

    // Kiểm tra hợp đồng có người thuê được liên kết hay không.
    @Query("""
            select count(ct) > 0
            from ContractTenant ct
            where ct.id.contract_id = :contractId
            """)
    boolean existsByContractId(
            @Param("contractId") UUID contractId
    );

    // Kiểm tra người thuê đã được liên kết với hợp đồng nào hay chưa.
    @Query("""
            select count(ct) > 0
            from ContractTenant ct
            where ct.tenant.tenant_id = :tenantId
            """)
    boolean existsByTenantId(
            @Param("tenantId") UUID tenantId
    );

    @Query("select count(ct) from ContractTenant ct where ct.id.contract_id = :contractId")
    long countByContractId(@Param("contractId") UUID contractId);

    // Lấy danh sách người thuê được đánh dấu là đại diện của một hợp đồng.
    @Query("""
            select ct
            from ContractTenant ct
            where ct.id.contract_id = :contractId
              and ct.is_representative = true
            """)
    List<ContractTenant> findRepresentativesByContractId(
            @Param("contractId") UUID contractId
    );
}
