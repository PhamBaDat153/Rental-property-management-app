package com.example.rentaly_managemen_tenant_app.models;

import java.time.LocalDate;
import java.util.UUID;

public class ContractTenant {
    public UUID contract_id;
    public UUID tenant_id;
    public Boolean is_representative;
    public LocalDate move_in_date;
    public LocalDate move_out_date;
    public String status;
}
