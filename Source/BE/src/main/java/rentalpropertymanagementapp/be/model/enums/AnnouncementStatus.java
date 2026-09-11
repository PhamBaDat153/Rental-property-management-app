package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum AnnouncementStatus {
    DRAFT("draft"),
    SCHEDULED("scheduled"),
    SENDING("sending"),
    SENT("sent"),
    CANCELLED("cancelled");

    @EnumeratedValue
    private final String value;

    AnnouncementStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
