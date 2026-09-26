package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public class RoomRequest {
    public UUID location_id;
    public String room_code;
    public String room_name;
    public Integer floor;
    public BigDecimal area_m2;
    public Integer max_occupants;
    public BigDecimal rent_price;
    public String status;
    public String description;

    public RoomRequest(UUID locationId, String code, String name, Integer floor, BigDecimal area,
                       Integer maxOccupants, BigDecimal rentPrice, String status, String description) {
        location_id = locationId;
        room_code = code;
        room_name = name;
        this.floor = floor;
        area_m2 = area;
        max_occupants = maxOccupants;
        rent_price = rentPrice;
        this.status = status;
        this.description = description;
    }
}
