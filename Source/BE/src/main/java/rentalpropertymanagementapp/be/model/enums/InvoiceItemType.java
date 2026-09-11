package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum InvoiceItemType {
    RENT("rent"),
    SERVICE("service"),
    ELECTRICITY("electricity"),
    WATER("water"),
    DISCOUNT("discount"),
    TAX("tax"),
    ADJUSTMENT("adjustment"),
    OTHER("other");

    @EnumeratedValue
    private final String value;

    InvoiceItemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
