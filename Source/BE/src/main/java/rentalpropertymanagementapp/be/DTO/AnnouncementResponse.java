package rentalpropertymanagementapp.be.DTO;
import rentalpropertymanagementapp.be.Model.Enum.AnnouncementStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record AnnouncementResponse(UUID announcement_id, String content, String announcement_type, AnnouncementStatus status, LocalDateTime sent_at, LocalDateTime created_at, List<UUID> recipient_ids) { }
