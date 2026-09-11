package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum MeterStatus {
    ACTIVE("active"),
    REPLACED("replaced"),
    BROKEN("broken"),
    INACTIVE("inactive");

    @EnumeratedValue
    private final String value;

    MeterStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
