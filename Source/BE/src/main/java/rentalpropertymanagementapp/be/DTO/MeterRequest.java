package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import java.util.UUID;

public record MeterRequest(@NotNull UUID room_id, @NotBlank String meter_type, @NotNull AvailableStatus status) { }
