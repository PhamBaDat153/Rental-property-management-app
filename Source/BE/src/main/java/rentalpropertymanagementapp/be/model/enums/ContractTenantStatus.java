package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum ContractTenantStatus {
    ACTIVE("active"),
    MOVED_OUT("moved_out");

    @EnumeratedValue
    private final String value;

    ContractTenantStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
