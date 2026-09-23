package rentalpropertymanagementapp.be.Model.Invoice;

import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Meter.MeterReading;
import rentalpropertymanagementapp.be.Model.Service.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "invoice_item",
        indexes = {
                @Index(name = "idx_invoice_item_invoice_id", columnList = "invoice_id"),
                @Index(name = "idx_invoice_item_service_id", columnList = "service_id"),
                @Index(name = "idx_invoice_item_reading_id", columnList = "reading_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {
    @Id
    @Column(name = "item_id", nullable = false)
    private UUID item_id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "reading_id")
    private MeterReading reading;

    @Column(length = 500)
    private String description;
    @Column(length = 50)
    private String unit;
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal unit_price;
    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal amount;

    @PrePersist
    public void prePersist() {
        if (item_id == null) item_id = com.fasterxml.uuid.Generators.timeBasedEpochGenerator().generate();
        if (unit_price == null) unit_price = BigDecimal.ZERO;
        if (amount == null) amount = BigDecimal.ZERO;
    }
}
