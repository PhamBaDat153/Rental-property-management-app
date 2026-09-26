package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;

import java.util.List;
import java.util.UUID;

public interface ContractTenantService {
    List<ContractTenant> getAll();
    ContractTenant get(UUID contractId, UUID tenantId);
    ContractTenant create(ContractTenantRequest request);
    ContractTenant update(UUID contractId, UUID tenantId, ContractTenantRequest request);
    void delete(UUID contractId, UUID tenantId);
}
