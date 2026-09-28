package com.android_dev.rentaly_management.DTO;

import java.time.LocalDate;
import java.util.UUID;

public class ContractTenantRequest {
    public UUID contract_id;
    public UUID tenant_id;
    public Boolean is_representative;
    public LocalDate move_in_date;
    public LocalDate move_out_date;

    public ContractTenantRequest(UUID contractId, UUID tenantId, Boolean representative,
                                 LocalDate moveInDate, LocalDate moveOutDate) {
        contract_id = contractId;
        tenant_id = tenantId;
        is_representative = representative;
        move_in_date = moveInDate;
        move_out_date = moveOutDate;
    }
}
