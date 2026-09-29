package com.android_dev.rentaly_management.DTO;
import java.math.BigDecimal;
import java.util.UUID;
public class InvoiceItem { public UUID item_id, service_id, reading_id; public String description, unit; public BigDecimal unit_price, amount; }
