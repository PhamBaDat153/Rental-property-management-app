package com.example.rentaly_managemen_tenant_app.models;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceItem {
    public UUID item_id;
    public UUID service_id;
    public UUID reading_id;
    public String description;
    public String unit;
    public BigDecimal unit_price;
    public BigDecimal amount;
}
