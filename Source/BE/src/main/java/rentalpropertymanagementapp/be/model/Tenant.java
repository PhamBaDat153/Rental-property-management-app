package rentalpropertymanagementapp.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import rentalpropertymanagementapp.be.model.enums.Gender;
import rentalpropertymanagementapp.be.model.enums.IdentityType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tenants",
        uniqueConstraints = @UniqueConstraint(name = "uq_tenants_user", columnNames = "user_id"),
        indexes = {
                @Index(name = "ix_tenants_phone", columnList = "phone"),
                @Index(name = "ix_tenants_identity_hash", columnList = "identity_number_hash")
        }
)
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME(3)")
    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "gender",
            nullable = false,
            columnDefinition = "ENUM('male','female','other','unknown') DEFAULT 'unknown'"
    )
    private Gender gender = Gender.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", columnDefinition = "ENUM('cccd','passport','other')")
    private IdentityType identityType;

    @Column(name = "identity_number_encrypted", length = 512, columnDefinition = "VARBINARY(512)")
    private byte[] identityNumberEncrypted;

    @Column(name = "identity_number_hash", length = 32, columnDefinition = "BINARY(32)")
    private byte[] identityNumberHash;

    @Column(name = "identity_issued_date")
    private LocalDate identityIssuedDate;

    @Column(name = "identity_issued_place", length = 255)
    private String identityIssuedPlace;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Lob
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
