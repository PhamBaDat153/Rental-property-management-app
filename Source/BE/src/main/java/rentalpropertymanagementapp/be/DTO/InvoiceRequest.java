package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceRequest(@NotNull UUID contract_id, @NotBlank String invoice_number, LocalDateTime issued_at,
        LocalDate due_date, @NotNull @DecimalMin("0.0") BigDecimal subtotal,
        @NotNull @DecimalMin("0.0") BigDecimal discount_amount, @NotNull @DecimalMin("0.0") BigDecimal tax_amount,
        @NotNull @DecimalMin("0.0") BigDecimal total_amount, String note, @NotNull InvoiceStatus status,
        @Valid List<InvoiceItemRequest> items) { }
