package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserRoleUpdateRequest(@NotBlank String role) {
}
