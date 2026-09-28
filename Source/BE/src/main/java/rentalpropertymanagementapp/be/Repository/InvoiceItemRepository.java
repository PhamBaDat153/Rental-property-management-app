package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import rentalpropertymanagementapp.be.Model.Invoice.InvoiceItem;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {
    @Query("select count(item) > 0 from InvoiceItem item where item.reading.reading_id = :readingId")
    boolean existsByReadingId(@Param("readingId") UUID readingId);
}
