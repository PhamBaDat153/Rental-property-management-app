package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class RentalContract {
    public UUID contract_id;
    public UUID room_id;
    public LocalDate start_date;
    public LocalDate end_date;
    public LocalDateTime signed_at;
    public BigDecimal rent_amount;
    public BigDecimal deposit_required;
    public Integer billing_day;
    public Integer payment_due_days;
    public String status;
    public String terms;
    public String document_url;
    public LocalDateTime terminated_at;
    public String termination_reason;
    public LocalDateTime created_at;
    public LocalDateTime updated_at;
}
