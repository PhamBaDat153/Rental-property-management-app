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

    public MeterReadingRequest(
            UUID meterId,
            LocalDateTime at,
            BigDecimal current,
            BigDecimal previous,
            BigDecimal used,
            String evidence,
            String readingNote
    ) {
        this.meter_id = meterId;
        this.reading_at = at;
        this.current_value = current;
        this.previous_value = previous;
        this.quantity = used;
        this.evidence_url = evidence;
        this.note = readingNote;
    }
}