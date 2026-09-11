package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum MaintenanceStatus {
    SUBMITTED("submitted"),
    ACCEPTED("accepted"),
    IN_PROGRESS("in_progress"),
    WAITING("waiting"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    REJECTED("rejected");

    @EnumeratedValue
    private final String value;

    MaintenanceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
