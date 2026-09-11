package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum UserStatus {
    ACTIVE("active"),
    LOCKED("locked"),
    DISABLED("disabled");

    @EnumeratedValue
    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
