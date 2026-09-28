package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

import java.math.BigDecimal;

public record ServiceRequest(
        @NotBlank String name,
        @NotBlank String unit,
        @NotBlank String calculation_method,
        @NotNull @DecimalMin("0.0") BigDecimal default_unit_price,
        @NotNull AvailableStatus room_status
) { }
