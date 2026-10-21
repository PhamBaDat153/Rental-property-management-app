package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceItemRequest {

    public UUID service_id;
    public UUID reading_id;

    public String description;
    public String unit;

    public BigDecimal unit_price;
    public BigDecimal amount;

    public InvoiceItemRequest(
            UUID service,
            String description,
            BigDecimal price,
            BigDecimal amount
    ) {
        this.service_id = service;
        this.description = description;
        this.unit_price = price;
        this.amount = amount;
    }

    public InvoiceItemRequest withUnit(String value) {
        this.unit = value;
        return this;
    }
}
