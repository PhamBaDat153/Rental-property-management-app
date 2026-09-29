package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemRequest(@NotNull UUID service_id, UUID reading_id, String description, String unit,
        @NotNull @DecimalMin("0.0") BigDecimal unit_price, @NotNull @DecimalMin("0.0") BigDecimal amount) { }
