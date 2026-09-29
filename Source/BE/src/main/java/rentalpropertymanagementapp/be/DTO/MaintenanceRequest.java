package rentalpropertymanagementapp.be.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import java.util.List;
import java.util.UUID;
public record MaintenanceRequest(@NotNull UUID user_id, @NotNull UUID room_id, @NotBlank String title, String description, @NotBlank String priority, @NotNull MaintenanceStatus status, List<String> image_urls) { }
