package rentalpropertymanagementapp.be.Model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.AuthProvider;
import rentalpropertymanagementapp.be.Model.ENUM.GeneralStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_login_email",
                        columnNames = "login_email"
                ),
                @UniqueConstraint(
                        name = "uq_user_auth_provider",
                        columnNames = {"auth_provider", "auth_provider_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID user_id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String user_name;

    @Column(name = "login_email", unique = true)
    private String login_email;

    @Column(name = "password_hash", nullable = true)
    private String password_hash;

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider auth_provider;

    @Column(name = "auth_provider_id")
    private String auth_provider_id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeneralStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_user_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        user_id = Generators.timeBasedEpochGenerator().generate();
        createdAt = LocalDateTime.now();
        status = GeneralStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
