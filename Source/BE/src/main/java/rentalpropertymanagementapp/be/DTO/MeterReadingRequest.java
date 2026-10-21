package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeterReadingRequest(@NotNull UUID meter_id, @NotNull LocalDateTime reading_at,
        @NotNull @Min(0) Long current_value,
        @NotNull @Min(0) Long previous_value,
        @NotNull @Min(0) Long quantity, String evidence_url, String note) { }
