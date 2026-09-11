package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum PaymentStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    @EnumeratedValue
    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
