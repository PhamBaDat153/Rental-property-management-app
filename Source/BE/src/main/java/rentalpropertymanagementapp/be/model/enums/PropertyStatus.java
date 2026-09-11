package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum PropertyStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    @EnumeratedValue
    private final String value;

    PropertyStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
