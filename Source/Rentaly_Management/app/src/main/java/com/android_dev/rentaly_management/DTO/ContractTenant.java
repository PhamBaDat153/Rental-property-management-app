package com.android_dev.rentaly_management.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContractTenant {
    public UUID contract_id;
    public UUID tenant_id;
    public Boolean is_representative;
    public LocalDate move_in_date;
    public LocalDate move_out_date;
    public String status;
    public LocalDateTime created_at;
    public LocalDateTime updated_at;
}
