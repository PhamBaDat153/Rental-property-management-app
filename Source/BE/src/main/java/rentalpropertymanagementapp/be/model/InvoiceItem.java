package rentalpropertymanagementapp.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import rentalpropertymanagementapp.be.model.enums.InvoiceItemType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "invoice_items",
        indexes = @Index(
                name = "ix_invoice_items_invoice_sort",
                columnList = "invoice_id, sort_order"
        )
)
public class InvoiceItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_from_id")
    private MeterReading meterReadingFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_to_id")
    private MeterReading meterReadingTo;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "item_type",
            nullable = false,
            columnDefinition = "ENUM('rent','service','electricity','water','discount','tax','adjustment','other')"
    )
    private InvoiceItemType itemType;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @PositiveOrZero
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @PositiveOrZero
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_details", columnDefinition = "JSON")
    private Map<String, Object> calculationDetails;

    @Column(name = "sort_order", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 0")
    private int sortOrder = 0;
}
