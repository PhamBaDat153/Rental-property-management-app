package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum ServiceType {
    ELECTRICITY("electricity"),
    WATER("water"),
    WIFI("wifi"),
    PARKING("parking"),
    GARBAGE("garbage"),
    CLEANING("cleaning"),
    OTHER("other");

    @EnumeratedValue
    private final String value;

    ServiceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
