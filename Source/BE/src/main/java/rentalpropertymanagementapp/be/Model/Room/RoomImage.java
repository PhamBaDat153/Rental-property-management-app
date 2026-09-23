package rentalpropertymanagementapp.be.Model.Room;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "image_url", nullable = false)
    private String image_url;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @PrePersist
    public void prePersist() {
        this.image_id = Generators.timeBasedGenerator().generate();
        this.created_at = LocalDateTime.now();
    }

}
