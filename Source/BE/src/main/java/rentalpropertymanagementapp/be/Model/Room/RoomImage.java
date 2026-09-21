package rentalpropertymanagementapp.be.Model.Room;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.ENUM.AvalibleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "room_image",
        indexes = {
                @Index(
                        name = "idx_room_image_room_id",
                        columnList = "room_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomImage {

    @Id
    @Column(name = "image_id", nullable = false)
    private UUID image_id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, optional = false)
    @JoinColumn(name = "room_room_id", nullable = false)
    private Room room;

    @Column(name = "image_url", nullable = false)
    private String image_url;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        image_id = Generators.timeBasedEpochGenerator().generate();
    }

}
