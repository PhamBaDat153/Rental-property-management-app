package rentalpropertymanagementapp.be.Model.Service;

import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Room.Room;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomService {
    @EmbeddedId
    private RoomServiceId id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("room_id")
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("service_id")
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(nullable = false)
    private Boolean is_active;
    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @PrePersist
    public void prePersist() {
        created_at = LocalDateTime.now();
        if (is_active == null) is_active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
