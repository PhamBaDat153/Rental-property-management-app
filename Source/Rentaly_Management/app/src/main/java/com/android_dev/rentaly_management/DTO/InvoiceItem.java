package com.android_dev.rentaly_management.DTO;

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