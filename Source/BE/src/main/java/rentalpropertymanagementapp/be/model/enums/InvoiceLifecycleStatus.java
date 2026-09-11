package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum InvoiceLifecycleStatus {
    DRAFT("draft"),
    ISSUED("issued"),
    CANCELLED("cancelled");

    @EnumeratedValue
    private final String value;

    InvoiceLifecycleStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
