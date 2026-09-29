package rentalpropertymanagementapp.be.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
public record AnnouncementRequest(@NotBlank String content, String announcement_type, boolean send, @NotEmpty List<UUID> recipient_ids) { }
