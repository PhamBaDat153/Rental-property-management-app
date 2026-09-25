package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rentalpropertymanagementapp.be.Model.User.Tenant;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
