package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractTenantResponse(UUID contract_id, UUID tenant_id, Boolean is_representative,
                                     LocalDate move_in_date, LocalDate move_out_date,
                                     ActiveStatus status, LocalDateTime created_at, LocalDateTime updated_at) {
    public static ContractTenantResponse from(ContractTenant assignment) {
        return new ContractTenantResponse(assignment.getId().getContract_id(), assignment.getId().getTenant_id(),
                assignment.getIs_representative(), assignment.getMove_in_date(), assignment.getMove_out_date(),
                assignment.getStatus(), assignment.getCreated_at(), assignment.getUpdated_at());
    }
}
