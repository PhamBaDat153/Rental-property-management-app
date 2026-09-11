package rentalpropertymanagementapp.be.model.enums;

import jakarta.persistence.EnumeratedValue;

public enum AttachmentFileType {
    IMAGE("image"),
    VIDEO("video"),
    DOCUMENT("document");

    @EnumeratedValue
    private final String value;

    AttachmentFileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
