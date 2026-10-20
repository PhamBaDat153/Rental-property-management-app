package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Invoice.InvoiceItem;

import java.util.UUID;

public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, UUID> {

    // Kiểm tra chỉ số công tơ đã được sử dụng trong chi tiết hóa đơn hay chưa.
    @Query("""
            select count(item) > 0
            from InvoiceItem item
            where item.reading.reading_id = :readingId
            """)
    boolean existsByReadingId(
            @Param("readingId") UUID readingId
    );
}