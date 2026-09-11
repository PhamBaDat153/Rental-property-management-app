package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum RoomOperationalStatus {
    AVAILABLE("available"),
    MAINTENANCE("maintenance"),
    INACTIVE("inactive");

    @EnumeratedValue
    private final String value;

    RoomOperationalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
