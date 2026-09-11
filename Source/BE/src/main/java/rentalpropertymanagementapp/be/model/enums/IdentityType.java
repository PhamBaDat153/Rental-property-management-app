package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum IdentityType {
    CCCD("cccd"),
    PASSPORT("passport"),
    OTHER("other");

    @EnumeratedValue
    private final String value;

    IdentityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
