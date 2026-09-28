package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Meter.Meter;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeterResponse(UUID meter_id, UUID room_id, String meter_type, AvailableStatus status,
                            LocalDateTime created_at, LocalDateTime updated_at) {
    public static MeterResponse from(Meter meter) {
        return new MeterResponse(meter.getMeter_id(), meter.getRoom().getRoom_id(), meter.getMeter_type(),
                meter.getStatus(), meter.getCreated_at(), meter.getUpdated_at());
    }
}
