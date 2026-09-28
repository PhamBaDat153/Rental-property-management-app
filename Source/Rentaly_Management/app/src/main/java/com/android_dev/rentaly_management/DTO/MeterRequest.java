package com.android_dev.rentaly_management.DTO;

import java.util.UUID;

public class MeterRequest {
    public UUID room_id;
    public String meter_type;
    public String status;
    public MeterRequest(UUID roomId, String type, String meterStatus) { room_id = roomId; meter_type = type; status = meterStatus; }
}
