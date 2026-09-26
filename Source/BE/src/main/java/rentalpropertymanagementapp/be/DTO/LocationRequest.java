package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;

public record LocationRequest(
        @NotBlank @Size(max = 50) String location_code,
        @NotBlank @Size(max = 255) String address_line,
        @Size(max = 100) String ward_name,
        @Size(max = 100) String district_name,
        @NotBlank @Size(max = 100) String province_name,
        AvailableStatus status,
        @Size(max = 2000) String description
) {
}
