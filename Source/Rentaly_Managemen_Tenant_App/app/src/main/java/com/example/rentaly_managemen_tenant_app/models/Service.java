package com.example.rentaly_managemen_tenant_app.models;

import java.math.BigDecimal;
import java.util.UUID;

public class Service {
    public UUID service_id;
    public String name;
    public String unit;
    public String calculation_method;
    public BigDecimal default_unit_price;
    public String room_status;
}
