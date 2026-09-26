package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.DTO.UserUpdateRequest;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    public List<UserTenantResponse> getUsers(String search);
    public Optional<User> authenticate(String username, String password, LoginType login_type);
    public Boolean authorize (User user,LoginType login_type);
    public UserTenantResponse getUserByID(UUID id);
    public UserTenantResponse createUser(UserCreateRequest request);
    public void deleteUserById(UUID id);
    public UserTenantResponse updateUserByID(UUID id, UserUpdateRequest request);

}
