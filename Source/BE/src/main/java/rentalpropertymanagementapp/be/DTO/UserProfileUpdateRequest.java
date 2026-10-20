package rentalpropertymanagementapp.be.DTO;

public record UserProfileUpdateRequest(
        String full_name,
        String date_of_birth,
        String phone,
        String email,
        String gender,
        String identityType,
        String identity_number,
        String identity_issued_date,
        String identity_issued_place,
        String permanent_address,
        String emergency_contact_name,
        String emergency_contact_phone,
        String additional_note
) {
}
