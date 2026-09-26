package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Room {
    public UUID room_id;
    public UUID location_id;
    public String room_code;
    public String room_name;
    public Integer floor;
    public BigDecimal area_m2;
    public Integer max_occupants;
    public BigDecimal rent_price;
    public String status;
    public String description;
    public List<String> image_urls;
}
