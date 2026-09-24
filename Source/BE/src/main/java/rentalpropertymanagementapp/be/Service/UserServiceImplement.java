package rentalpropertymanagementapp.be.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImplement implements UserService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImplement(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Boolean authenticate(String username, String password, LoginType loginType) {
        if (username == null || password == null || loginType == null) {
            return false;
        }

        User user = userRepository.findByUser_nameLike(username);
        return user != null
                && user.getStatus() == ActiveStatus.ACTIVE
                && user.getPassword_hash() != null
                && passwordEncoder.matches(password, user.getPassword_hash())
                && authorize(user, loginType);
    }

    @Override
    public Boolean authorize(User user, LoginType loginType) {
        if (user == null || loginType == null || user.getRole() == null) {
            return false;
        }

        Role role = user.getRole();
        if (role.getStatus() != ActiveStatus.ACTIVE
                || role.getRole_name() == null) {
            return false;
        }

        String requiredRole = switch (loginType) {
            case Manage -> "LANDLORD";
            case Use -> "TENANT";
        };

        return requiredRole.equalsIgnoreCase(role.getRole_name());
    }
}
