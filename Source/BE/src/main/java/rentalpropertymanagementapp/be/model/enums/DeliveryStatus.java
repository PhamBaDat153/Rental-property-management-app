package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum DeliveryStatus {
    PENDING("pending"),
    SENT("sent"),
    DELIVERED("delivered"),
    FAILED("failed");

    @EnumeratedValue
    private final String value;

    DeliveryStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
