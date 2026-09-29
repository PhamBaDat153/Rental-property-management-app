package rentalpropertymanagementapp.be.DTO;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record MaintenanceResponse(UUID request_id, UUID user_id, UUID room_id, String title, String description, String priority, MaintenanceStatus status, LocalDateTime created_at, LocalDateTime updated_at, LocalDateTime completed_at, List<String> image_urls) { }
