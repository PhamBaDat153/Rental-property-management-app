package rentalpropertymanagementapp.be.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Repository.RoleRepository;

import java.util.List;

@Service
public class RoleServiceImplement implements RoleService {

    @Autowired
    private final RoleRepository roleRepository;

    public RoleServiceImplement(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }
}
