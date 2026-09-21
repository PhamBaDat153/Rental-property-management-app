package rentalpropertymanagementapp.be.Model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.GeneralStatus;
import rentalpropertymanagementapp.be.Model.Room.Room;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "property",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_property_code", columnNames = {"property_code"})
        },
        indexes = {
                @Index(
                        name = "idx_property_location",
                        columnList = "province_name, district_name, ward_name"
                )
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Property {

        @Id
        @Column(name = "property_id", nullable = false)
        private UUID property_id;

        @Column(name = "property_code", nullable = false, unique = true, length = 50)
        private String property_code;

        @Column(name = "address_line", nullable = false)
        private String address_line;

        @Column(name = "ward_name", nullable = false, length = 100)
        private String ward_name;

        @Column(name = "district_name", nullable = false, length = 100)
        private String district_name;

        @Column(name = "province_name", nullable = false)
        private String province_name;

        @Column(name = "description")
        private String description;

        @Enumerated
        @Column(name = "status", nullable = false)
        private GeneralStatus status;

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at",nullable = false)
        private LocalDateTime updatedAt;

        @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Room> rooms = new LinkedHashSet<>();

        @PrePersist
        public void prePersist() {
                createdAt = LocalDateTime.now();
                status = GeneralStatus.ACTIVE;
                property_id = Generators.timeBasedEpochGenerator().generate();
        }

        @PreUpdate
        public void preUpdate() {
                updatedAt = LocalDateTime.now();
        }
}
