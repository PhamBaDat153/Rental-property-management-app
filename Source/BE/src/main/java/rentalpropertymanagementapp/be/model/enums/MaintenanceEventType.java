package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum MaintenanceEventType {
    CREATED("created"),
    ACCEPTED("accepted"),
    STATUS_CHANGED("status_changed"),
    NOTE_ADDED("note_added"),
    ASSIGNED("assigned"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    REJECTED("rejected"),
    TENANT_CONFIRMED("tenant_confirmed");

    @EnumeratedValue
    private final String value;

    MaintenanceEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
