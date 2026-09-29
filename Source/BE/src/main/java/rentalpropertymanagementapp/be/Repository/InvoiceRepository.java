package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;

import java.util.UUID;
import java.util.List;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    @Query("select count(i) > 0 from Invoice i where i.contract.contract_id = :contractId")
    boolean existsByContractId(@Param("contractId") UUID contractId);
    @Query("select count(i) > 0 from Invoice i where i.invoice_number = :invoiceNumber")
    boolean existsByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);
    @Query("select i from Invoice i where (:contractId is null or i.contract.contract_id = :contractId) and (:status is null or i.status = :status) order by i.issued_at desc, i.invoice_id desc")
    List<Invoice> search(@Param("contractId") UUID contractId, @Param("status") InvoiceStatus status);
}
