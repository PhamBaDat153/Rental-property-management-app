package rentalpropertymanagementapp.be.DTO;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record InvoiceResponse(UUID invoice_id, UUID contract_id, String invoice_number, LocalDateTime issued_at, LocalDate due_date, BigDecimal subtotal, BigDecimal discount_amount, BigDecimal tax_amount, BigDecimal total_amount, String note, InvoiceStatus status, List<InvoiceItemResponse> items) { }
