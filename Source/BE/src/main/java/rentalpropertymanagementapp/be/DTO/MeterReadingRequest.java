package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeterReadingRequest(@NotNull UUID meter_id, @NotNull LocalDateTime reading_at,
        @NotNull @DecimalMin("0.0") BigDecimal current_value,
        @NotNull @DecimalMin("0.0") BigDecimal previous_value,
        @NotNull @DecimalMin("0.0") BigDecimal quantity, String evidence_url, String note) { }
