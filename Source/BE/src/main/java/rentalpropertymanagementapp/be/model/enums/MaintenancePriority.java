package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum MaintenancePriority {
    LOW("low"),
    NORMAL("normal"),
    HIGH("high"),
    URGENT("urgent");

    @EnumeratedValue
    private final String value;

    MaintenancePriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
