package com.android_dev.rentaly_management.DTO;
import java.math.BigDecimal; import java.util.UUID;
public class InvoiceItemRequest { public UUID service_id, reading_id; public String description, unit; public BigDecimal unit_price, amount; public InvoiceItemRequest(UUID service, String description, BigDecimal price, BigDecimal amount) { service_id=service; this.description=description; unit_price=price; this.amount=amount; } }
