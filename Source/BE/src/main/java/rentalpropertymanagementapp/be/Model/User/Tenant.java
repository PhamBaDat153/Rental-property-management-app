package rentalpropertymanagementapp.be.Model.User;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.IdentityType;
import rentalpropertymanagementapp.be.Model.Enum.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tenant", indexes = {
        @Index(name = "idx_tenant_phone", columnList = "phone"),
        @Index(name = "idx_tenant_full_name", columnList = "full_name")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_tenant_identity", columnNames = {"identity_type", "identity_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private UUID tenant_id;

    @Column(name = "full_name", nullable = false)
    private String full_name;

    @Column(name = "date_of_birth")
    private LocalDate date_of_birth;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "avatar_url")
    private String avatar_url;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type")
    private IdentityType identityType;

    @Column(name = "identity_number", length = 30)
    private String identity_number;

    @Column(name = "identity_issued_date")
    private LocalDate identity_issued_date;

    @Column(name = "identity_issued_place")
    private String identity_issued_place;

    @Column(name = "permanent_address")
    private String permanent_address;

    @Column(name = "emergency_contact_name")
    private String emergency_contact_name;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergency_contact_phone;

    @Column(name = "additional_note")
    private String additional_note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @OneToOne(mappedBy = "tenant", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, orphanRemoval = true)
    private User user;

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
        this.tenant_id = Generators.timeBasedEpochGenerator().generate();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }

}
