package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum AnnouncementTargetType {
    ALL("all"),
    PROPERTY("property"),
    ROOM("room"),
    CONTRACT("contract"),
    SELECTED_TENANTS("selected_tenants");

    @EnumeratedValue
    private final String value;

    AnnouncementTargetType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
