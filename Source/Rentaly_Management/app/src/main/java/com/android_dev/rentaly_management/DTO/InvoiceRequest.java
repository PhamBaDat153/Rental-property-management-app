package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InvoiceRequest {

    public UUID contract_id;

    public String invoice_number;
    public String note;
    public String status;

    public LocalDateTime issued_at;
    public LocalDate due_date;

    public BigDecimal subtotal;
    public BigDecimal discount_amount;
    public BigDecimal tax_amount;
    public BigDecimal total_amount;

    public List<InvoiceItemRequest> items;

    public InvoiceRequest(
            UUID contract,
            String number,
            LocalDate due,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total,
            String note,
            String status
    ) {
        this.contract_id = contract;
        this.invoice_number = number;
        this.due_date = due;
        this.subtotal = subtotal;
        this.discount_amount = discount;
        this.tax_amount = tax;
        this.total_amount = total;
        this.note = note;
        this.status = status;
    }
}