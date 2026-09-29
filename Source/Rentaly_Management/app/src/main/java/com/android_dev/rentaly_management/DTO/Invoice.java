package com.android_dev.rentaly_management.DTO;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
public class Invoice { public UUID invoice_id, contract_id; public String invoice_number, note, status; public LocalDateTime issued_at; public LocalDate due_date; public BigDecimal subtotal, discount_amount, tax_amount, total_amount; public List<InvoiceItem> items; }
