package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.User;

import java.util.List;

public interface UserService {
    public List<User> getUsers();
    public Boolean authenticate(String username, String password, LoginType login_type);
    public Boolean authorize (User user,LoginType login_type);
}
