package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.RoomRequest;
import rentalpropertymanagementapp.be.Model.Room.Room;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<Room> getAll();
    Room get(UUID id);
    Room create(RoomRequest request, MultipartFile[] images);
    Room update(UUID id, RoomRequest request, MultipartFile[] images);
    void delete(UUID id);
}
