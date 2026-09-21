package rentalpropertymanagementapp.be.Model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.GeneralStatus;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table (name = "role",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_role_name",
                        columnNames = "role_name"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID role_id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String role_name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeneralStatus status;

    @ManyToMany(mappedBy = "roles", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private Set<User> users = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        role_id = Generators.timeBasedEpochGenerator().generate();
        status = GeneralStatus.ACTIVE;
    }
}
