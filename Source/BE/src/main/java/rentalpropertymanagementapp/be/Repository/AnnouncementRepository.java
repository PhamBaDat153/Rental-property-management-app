package rentalpropertymanagementapp.be.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rentalpropertymanagementapp.be.Model.Announcement.Announcement;
import java.util.UUID;
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> { }
