package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceRequest {
    public String name;
    public String unit;
    public String calculation_method;
    public BigDecimal default_unit_price;
    public String room_status;
    public ServiceRequest(String name, String unit, String calculationMethod, BigDecimal price, String status) { this.name = name; this.unit = unit; calculation_method = calculationMethod; default_unit_price = price; room_status = status; }
}
