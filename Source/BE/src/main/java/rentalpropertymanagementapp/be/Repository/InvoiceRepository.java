package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // Kiểm tra hợp đồng đã có hóa đơn hay chưa.
    @Query("""
            select count(i) > 0
            from Invoice i
            where i.contract.contract_id = :contractId
            """)
    boolean existsByContractId(
            @Param("contractId") UUID contractId
    );

    // Kiểm tra số hóa đơn đã tồn tại trong hệ thống hay chưa.
    @Query("""
            select count(i) > 0
            from Invoice i
            where i.invoice_number = :invoiceNumber
            """)
    boolean existsByInvoiceNumber(
            @Param("invoiceNumber") String invoiceNumber
    );

    // Tìm kiếm hóa đơn theo hợp đồng và trạng thái.
    // Nếu contractId hoặc status là null, bỏ qua điều kiện lọc tương ứng.
    // Sắp xếp hóa đơn theo ngày phát hành giảm dần,
    // sau đó theo UUID hóa đơn giảm dần.
    @Query("""
            select i
            from Invoice i
            where (:contractId is null
                   or i.contract.contract_id = :contractId)
              and (:status is null
                   or i.status = :status)
            order by i.issued_at desc, i.invoice_id desc
            """)
    List<Invoice> search(
            @Param("contractId") UUID contractId,
            @Param("status") InvoiceStatus status
    );
}