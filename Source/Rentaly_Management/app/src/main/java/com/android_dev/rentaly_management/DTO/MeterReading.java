package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MeterReading {
    public UUID reading_id;
    public UUID meter_id;
    public LocalDateTime reading_at;
    public BigDecimal current_value;
    public BigDecimal previous_value;
    public BigDecimal quantity;
    public String evidence_url;
    public String note;
}
