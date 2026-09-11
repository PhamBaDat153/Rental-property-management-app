package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum PaymentMethod {
    CASH("cash"),
    BANK_TRANSFER("bank_transfer"),
    E_WALLET("e_wallet"),
    OTHER("other");

    @EnumeratedValue
    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
