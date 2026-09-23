package rentalpropertymanagementapp.be.Service;

import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.Model.User.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleService {

    public List<Role> getRoles();
}
