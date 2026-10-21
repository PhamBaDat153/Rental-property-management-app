package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;

public class Invoice implements Serializable {

    public UUID invoice_id;
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

    public List<InvoiceItem> items;
}