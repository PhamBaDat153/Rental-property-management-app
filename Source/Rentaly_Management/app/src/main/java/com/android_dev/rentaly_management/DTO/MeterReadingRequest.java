package com.android_dev.rentaly_management.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MeterReadingRequest {
    public UUID meter_id;
    public LocalDateTime reading_at;
    public BigDecimal current_value;
    public BigDecimal previous_value;
    public BigDecimal quantity;
    public String evidence_url;
    public String note;
    public MeterReadingRequest(UUID meterId, LocalDateTime at, BigDecimal current, BigDecimal previous, BigDecimal used, String evidence, String readingNote) {
        meter_id = meterId; reading_at = at; current_value = current; previous_value = previous; quantity = used; evidence_url = evidence; note = readingNote;
    }
}
