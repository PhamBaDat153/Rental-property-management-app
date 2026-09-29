package rentalpropertymanagementapp.be.Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Model.Maintenance.MaintenanceRequest;
import rentalpropertymanagementapp.be.Repository.*;
import java.time.LocalDateTime;
import java.util.*;
@Service public class MaintenanceResourceService {
    private final MaintenanceRequestRepository requests; private final UserRepository users; private final RoomRepository rooms;
    public MaintenanceResourceService(MaintenanceRequestRepository requests, UserRepository users, RoomRepository rooms) { this.requests = requests; this.users = users; this.rooms = rooms; }
    public List<MaintenanceRequest> list(UUID roomId, MaintenanceStatus status, String priority) { return requests.search(roomId, status, priority); }
    public MaintenanceRequest get(UUID id) { return requests.findById(id).orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found")); }
    public MaintenanceRequest save(UUID id, rentalpropertymanagementapp.be.DTO.MaintenanceRequest request) { MaintenanceRequest value = id == null ? new MaintenanceRequest() : get(id); value.setUser(users.findById(request.user_id()).orElseThrow(() -> new ResourceNotFoundException("User not found"))); value.setRoom(rooms.findById(request.room_id()).orElseThrow(() -> new ResourceNotFoundException("Room not found"))); value.setTitle(request.title().trim()); value.setDescription(request.description()); value.setPriority(request.priority().trim().toUpperCase()); value.setStatus(request.status()); value.setCompleted_at(request.status() == MaintenanceStatus.COMPLETED ? (value.getCompleted_at() == null ? LocalDateTime.now() : value.getCompleted_at()) : null); return requests.save(value); }
    @Transactional public void delete(UUID id) { requests.delete(get(id)); }
    public MaintenanceResponse response(MaintenanceRequest value) { return new MaintenanceResponse(value.getRequest_id(), value.getUser().getUser_id(), value.getRoom().getRoom_id(), value.getTitle(), value.getDescription(), value.getPriority(), value.getStatus(), value.getCreated_at(), value.getUpdated_at(), value.getCompleted_at(), value.getImages().stream().map(i -> i.getFile_url()).toList()); }
}
