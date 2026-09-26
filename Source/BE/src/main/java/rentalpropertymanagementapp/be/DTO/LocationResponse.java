package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Room.Location;

import java.time.LocalDateTime;
import java.util.UUID;

public record LocationResponse(
        UUID location_id,
        String location_code,
        String address_line,
        String ward_name,
        String district_name,
        String province_name,
        String description,
        AvailableStatus status,
        LocalDateTime created_at,
        LocalDateTime updated_at
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getLocation_id(), location.getLocation_code(), location.getAddress_line(),
                location.getWard_name(), location.getDistrict_name(), location.getProvince_name(), location.getDescription(),
                location.getStatus(), location.getCreated_at(), location.getUpdated_at()
        );
    }
}
