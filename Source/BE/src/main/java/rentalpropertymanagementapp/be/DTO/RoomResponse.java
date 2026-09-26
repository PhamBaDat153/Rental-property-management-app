package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Room.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public record RoomResponse(UUID room_id, UUID location_id, String room_code, String room_name,
                           Integer floor, BigDecimal area_m2, Integer max_occupants,
                           BigDecimal rent_price, AvailableStatus status, String description,
                           LocalDateTime created_at, LocalDateTime updated_at, List<String> image_urls) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getRoom_id(), room.getLocation().getLocation_id(), room.getRoom_code(),
                room.getRoom_name(), room.getFloor(), room.getArea_m2(), room.getMax_occupants(),
                room.getRent_price(), room.getStatus(), room.getDescription(), room.getCreated_at(), room.getUpdated_at(),
                room.getImages() == null ? List.of() : room.getImages().stream().map(image -> image.getImage_url()).toList());
    }
}
