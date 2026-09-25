package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Enum.Gender;
import rentalpropertymanagementapp.be.Model.Enum.IdentityType;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserTenantResponse(
        UUID user_id,
        String user_name,
        String role,
        ActiveStatus status,
        UUID tenant_id,
        String full_name,
        LocalDate date_of_birth,
        String phone,
        String email,
        Gender gender,
        String avatar_url,
        IdentityType identityType,
        String identity_number,
        LocalDate identity_issued_date,
        String identity_issued_place,
        String permanent_address,
        String emergency_contact_name,
        String emergency_contact_phone,
        String additional_note,
        LocalDateTime tenant_created_at,
        LocalDateTime tenant_updated_at
) {
}
