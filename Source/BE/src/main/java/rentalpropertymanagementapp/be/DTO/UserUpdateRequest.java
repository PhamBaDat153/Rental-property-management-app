package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(@NotBlank String role, @NotBlank String status) {
}
