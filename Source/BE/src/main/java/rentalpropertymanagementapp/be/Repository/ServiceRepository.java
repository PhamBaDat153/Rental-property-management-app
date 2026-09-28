package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Service.Service;

public interface ServiceRepository extends JpaRepository<Service, UUID> { }
