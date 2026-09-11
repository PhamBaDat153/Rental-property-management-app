package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum CalculationMethod {
    METERED("metered"),
    FIXED("fixed"),
    PER_PERSON("per_person"),
    PER_VEHICLE("per_vehicle"),
    PER_UNIT("per_unit");

    @EnumeratedValue
    private final String value;

    CalculationMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
