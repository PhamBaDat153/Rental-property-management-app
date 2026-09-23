package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rentalpropertymanagementapp.be.Model.User.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}