package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenantId;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.User.Tenant;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.TenantRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ContractTenantServiceImplement implements ContractTenantService {
    private final ContractTenantRepository repository;
    private final RentalContractRepository contractRepository;
    private final TenantRepository tenantRepository;

    public ContractTenantServiceImplement(ContractTenantRepository repository, RentalContractRepository contractRepository, TenantRepository tenantRepository) {
        this.repository = repository; this.contractRepository = contractRepository; this.tenantRepository = tenantRepository;
    }

    public List<ContractTenant> getAll() { return repository.findAll(); }
    public ContractTenant get(UUID contractId, UUID tenantId) { return repository.findById(new ContractTenantId(contractId, tenantId)).orElseThrow(() -> new ResourceNotFoundException("Contract tenant assignment not found")); }

    @Transactional
    public ContractTenant create(ContractTenantRequest request) {
        validateDates(request.move_in_date(), request.move_out_date());
        ContractTenantId id = new ContractTenantId(request.contract_id(), request.tenant_id());
        if (repository.existsById(id)) throw new ResourceConflictException("Tenant is already assigned to contract");
        RentalContract contract = contractRepository.findById(request.contract_id()).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        Tenant tenant = tenantRepository.findById(request.tenant_id()).orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        return repository.save(toEntity(request, id, contract, tenant));
    }

    @Transactional
    public ContractTenant update(UUID contractId, UUID tenantId, ContractTenantRequest request) {
        if (!contractId.equals(request.contract_id()) || !tenantId.equals(request.tenant_id())) throw new ResourceConflictException("Assignment identifiers cannot change");
        validateDates(request.move_in_date(), request.move_out_date());
        ContractTenant assignment = get(contractId, tenantId);
        assignment.setIs_representative(request.is_representative()); assignment.setMove_in_date(request.move_in_date()); assignment.setMove_out_date(request.move_out_date());
        return repository.save(assignment);
    }

    @Transactional
    public void delete(UUID contractId, UUID tenantId) { get(contractId, tenantId); repository.deleteById(new ContractTenantId(contractId, tenantId)); }

    private ContractTenant toEntity(ContractTenantRequest request, ContractTenantId id, RentalContract contract, Tenant tenant) {
        return ContractTenant.builder().id(id).rentalContract(contract).tenant(tenant).is_representative(request.is_representative())
                .move_in_date(request.move_in_date()).move_out_date(request.move_out_date()).build();
    }
    private void validateDates(LocalDate start, LocalDate end) { if (end != null && start != null && end.isBefore(start)) throw new ResourceConflictException("Move-out date must not precede move-in date"); }
}
