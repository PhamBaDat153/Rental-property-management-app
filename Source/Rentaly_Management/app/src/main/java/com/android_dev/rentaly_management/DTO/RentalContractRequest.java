package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class RentalContractRequest {
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

    public RentalContractRequest(UUID roomId, LocalDate startDate, LocalDate endDate,
                                 LocalDateTime signedAt, BigDecimal rentAmount, BigDecimal depositRequired,
                                 Integer billingDay, Integer paymentDueDays, String status,
                                 String terms, LocalDateTime terminatedAt, String terminationReason) {
        room_id = roomId;
        start_date = startDate;
        end_date = endDate;
        signed_at = signedAt;
        rent_amount = rentAmount;
        deposit_required = depositRequired;
        billing_day = billingDay;
        this.payment_due_days = paymentDueDays;
        this.status = status;
        this.terms = terms;
        terminated_at = terminatedAt;
        termination_reason = terminationReason;
    }
}
