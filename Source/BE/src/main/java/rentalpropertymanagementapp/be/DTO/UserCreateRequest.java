package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String user_name,
        @NotBlank String password
) {
}
