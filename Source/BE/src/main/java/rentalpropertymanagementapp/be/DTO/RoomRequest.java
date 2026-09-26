package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomRequest(
        @NotNull UUID location_id,
        @NotBlank @Size(max = 50) String room_code,
        @Size(max = 100) String room_name,
        Integer floor,
        @DecimalMin("0.0") BigDecimal area_m2,
        @NotNull @Min(1) Integer max_occupants,
        @NotNull @DecimalMin("0.0") BigDecimal rent_price,
        @NotNull AvailableStatus status,
        String description
) {
}
