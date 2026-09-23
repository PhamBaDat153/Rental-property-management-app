package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rentalpropertymanagementapp.be.Model.User.Role;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}