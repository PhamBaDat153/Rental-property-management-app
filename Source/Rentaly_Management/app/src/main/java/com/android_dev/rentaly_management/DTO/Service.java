package com.android_dev.rentaly_management.DTO;

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
