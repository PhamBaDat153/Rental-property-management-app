package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum ContractStatus {
    DRAFT("draft"),
    ACTIVE("active"),
    EXPIRED("expired"),
    TERMINATED("terminated"),
    CANCELLED("cancelled");

    @EnumeratedValue
    private final String value;

    ContractStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
