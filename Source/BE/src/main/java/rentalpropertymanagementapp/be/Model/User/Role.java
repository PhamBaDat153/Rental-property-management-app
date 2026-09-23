package rentalpropertymanagementapp.be.Model.User;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "role",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_role_name",
                        columnNames = "role_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID role_id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String role_name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActiveStatus status;

    @PrePersist
    public void prePersist() {
        this.role_id = Generators.timeBasedEpochGenerator().generate();
        status = ActiveStatus.ACTIVE;
    }

}
