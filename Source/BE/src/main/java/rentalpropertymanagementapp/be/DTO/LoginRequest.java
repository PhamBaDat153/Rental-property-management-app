package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull LoginType loginType
) {
}
