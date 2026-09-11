package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum AuthProvider {
    GOOGLE("google");

    @EnumeratedValue
    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
