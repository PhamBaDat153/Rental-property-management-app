package rentalpropertymanagementapp.be.Model.Tenant;

import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.Gender;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.User;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "tenant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_tenant_user_id",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uq_tenant_identity",
                        columnNames = {"identity_type", "identity_number"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_tenant_room_id",
                        columnList = "room_id"
                ),
                @Index(
                        name = "idx_tenant_phone",
                        columnList = "phone"
                ),
                @Index(
                        name = "idx_tenant_full_name",
                        columnList = "full_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tenant_id", nullable = false)
    private UUID tenant_id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, orphanRemoval = true)
    @JoinColumn(name = "user_user_id", nullable = false)
    private User user;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "room_room_id")
    private Room room;

    @Column(name = "full_name", nullable = false)
    private String full_name;

    @Column(name = "date_of_birth")
    private LocalDate date_of_birth;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Enumerated
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "avatar_url")
    private String avatar_url;



}
