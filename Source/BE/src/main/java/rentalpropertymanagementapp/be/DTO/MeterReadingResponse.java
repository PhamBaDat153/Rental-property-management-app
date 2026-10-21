package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Meter.MeterReading;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeterReadingResponse(UUID reading_id, UUID meter_id, LocalDateTime reading_at,
        Long current_value, Long previous_value, Long quantity,
        String evidence_url, String note, LocalDateTime created_at, LocalDateTime updated_at) {
    public static MeterReadingResponse from(MeterReading reading) {
        return new MeterReadingResponse(reading.getReading_id(), reading.getMeter().getMeter_id(), reading.getReading_at(),
                reading.getCurrent_value(), reading.getPrevious_value(), reading.getQuantity(), reading.getEvidence_url(),
                reading.getNote(), reading.getCreated_at(), reading.getUpdated_at());
    }
}
