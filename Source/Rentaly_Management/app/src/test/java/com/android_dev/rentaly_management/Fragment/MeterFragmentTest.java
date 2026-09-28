package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.MeterReading;
import com.android_dev.rentaly_management.DTO.MeterReadingRequest;
import com.android_dev.rentaly_management.DTO.MeterRequest;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MeterFragmentTest {
    @Test public void readingValidationRejectsNegativeValues() {
        assertTrue(MeterFragment.validReadingValues(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        assertFalse(MeterFragment.validReadingValues(new BigDecimal("-0.1"), BigDecimal.ZERO, BigDecimal.ZERO));
        assertFalse(MeterFragment.validReadingValues(null, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test public void readingDisplayIncludesConsumptionFields() {
        MeterReading reading = new MeterReading();
        reading.reading_at = LocalDateTime.parse("2026-09-28T10:00:00");
        reading.previous_value = BigDecimal.TEN;
        reading.current_value = new BigDecimal("12.5");
        reading.quantity = new BigDecimal("2.5");
        assertEquals("2026-09-28T10:00 | Cũ: 10 | Mới: 12.5 | Dùng: 2.5", MeterFragment.readingLabel(reading));
    }

    @Test public void requestsKeepSelectedRoomAndMeterIds() {
        UUID roomId = UUID.randomUUID();
        UUID meterId = UUID.randomUUID();
        MeterRequest meter = new MeterRequest(roomId, "ELECTRICITY", "AVAILABLE");
        MeterReadingRequest reading = new MeterReadingRequest(meterId, LocalDateTime.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, null, null);
        assertEquals(roomId, meter.room_id);
        assertEquals(meterId, reading.meter_id);
    }
}
