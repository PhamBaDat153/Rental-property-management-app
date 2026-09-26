package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RentalContractRequest(
        @NotNull UUID room_id,
        @NotNull LocalDate start_date,
        LocalDate end_date,
        @NotNull @DecimalMin("0.0") BigDecimal rent_amount,
        @NotNull @DecimalMin("0.0") BigDecimal deposit_required,
        @Min(1) @Max(31) Integer billing_day,
        @NotNull @Min(0) Integer payment_due_days,
        @NotNull ActiveStatus status,
        String terms,
        String document_url
) {
}
