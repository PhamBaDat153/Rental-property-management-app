package rentalpropertymanagementapp.be.DTO;
import java.math.BigDecimal;
import java.util.UUID;
public record InvoiceItemResponse(UUID item_id, UUID service_id, UUID reading_id, String description, String unit, BigDecimal unit_price, BigDecimal amount) { }
