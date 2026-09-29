package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.User.User;

import java.util.UUID;

public record LoginResponse(UUID user_id, String user_name) {
    public static LoginResponse from(User user) {
        return new LoginResponse(user.getUser_id(), user.getUser_name());
    }
}
