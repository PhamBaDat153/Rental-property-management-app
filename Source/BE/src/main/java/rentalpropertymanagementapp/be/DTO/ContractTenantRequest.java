package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ContractTenantRequest(@NotNull UUID contract_id, @NotNull UUID tenant_id,
                                    @NotNull Boolean is_representative, LocalDate move_in_date,
                                    LocalDate move_out_date) {
}
