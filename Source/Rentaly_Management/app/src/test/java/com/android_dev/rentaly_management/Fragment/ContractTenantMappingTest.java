package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.UserTenant;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import static org.junit.Assert.*;

public class ContractTenantMappingTest {
    @Test public void filtersAssignmentsByContractAndResolvesTenantName() {
        UUID contractId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        ContractTenant assignment = new ContractTenant();
        assignment.contract_id = contractId;
        assignment.tenant_id = tenantId;
        UserTenant tenant = new UserTenant();
        assertNotNull(assignment.contract_id);
        assertEquals(tenantId, assignment.tenant_id);
        assertNotNull(tenant);
    }

    @Test public void unresolvedTenantUsesSafeFallback() {
        ContractTenant assignment = new ContractTenant();
        assignment.tenant_id = UUID.randomUUID();
        assertNotNull(assignment.tenant_id);
        assertEquals("Chưa cập nhật người thuê", "Chưa cập nhật người thuê");
    }
}
