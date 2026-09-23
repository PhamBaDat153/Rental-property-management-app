package rentalpropertymanagementapp.be.Model.Service;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoomServiceId implements Serializable {

    private UUID room_id;
    private UUID service_id;
}
