package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.InvoiceItemRequest;
import rentalpropertymanagementapp.be.DTO.InvoiceItemResponse;
import rentalpropertymanagementapp.be.DTO.InvoiceRequest;
import rentalpropertymanagementapp.be.DTO.InvoiceResponse;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;
import rentalpropertymanagementapp.be.Model.Invoice.InvoiceItem;
import rentalpropertymanagementapp.be.Repository.InvoiceRepository;
import rentalpropertymanagementapp.be.Repository.MeterReadingRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.ServiceRepository;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceServiceImplement implements InvoiceService {

    private final InvoiceRepository invoices;
    private final RentalContractRepository contracts;
    private final ServiceRepository services;
    private final MeterReadingRepository readings;

    // Khởi tạo service và tiêm các Repository cần thiết.
    public InvoiceServiceImplement(
            InvoiceRepository invoices,
            RentalContractRepository contracts,
            ServiceRepository services,
            MeterReadingRepository readings
    ) {
        this.invoices = invoices;
        this.contracts = contracts;
        this.services = services;
        this.readings = readings;
    }

    // -------------------------------------------------------------------------
    // Invoice retrieval
    // -------------------------------------------------------------------------

    // Lấy danh sách hóa đơn, có thể lọc theo hợp đồng và trạng thái.
    @Override
    public List<Invoice> list(
            UUID contractId,
            InvoiceStatus status
    ) {
        return invoices.search(contractId, status);
    }

    // Tìm hóa đơn theo UUID.
    // Nếu không tìm thấy, ném ResourceNotFoundException.
    @Override
    public Invoice get(UUID id) {
        return invoices.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Invoice not found"
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Create and update invoice
    // -------------------------------------------------------------------------

    /**
     * Tạo mới hoặc cập nhật hóa đơn.
     * Nếu id null, tạo hóa đơn mới và kiểm tra số hóa đơn không bị trùng.
     * Nếu id khác null, lấy hóa đơn hiện có để cập nhật.
     * Kiểm tra hợp đồng, dịch vụ và chỉ số công tơ được tham chiếu tồn tại.
     * Thay thế danh sách chi tiết hóa đơn bằng dữ liệu trong request,
     * sau đó lưu hóa đơn và các chi tiết liên quan trong một transaction.
     */
    @Override
    @Transactional
    public Invoice save(UUID id, InvoiceRequest request) {
        Invoice invoice = id == null
                ? new Invoice()
                : get(id);

        if (id == null
                && invoices.existsByInvoiceNumber(
                request.invoice_number().trim()
        )) {
            throw new ResourceConflictException(
                    "Invoice number already exists"
            );
        }

        RentalContract contract = contracts.findById(
                request.contract_id()
        ).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Contract not found"
                )
        );

        invoice.setContract(contract);
        invoice.setInvoice_number(request.invoice_number().trim());
        invoice.setIssued_at(request.issued_at());
        invoice.setDue_date(request.due_date());
        invoice.setSubtotal(request.subtotal());
        invoice.setDiscount_amount(request.discount_amount());
        invoice.setTax_amount(request.tax_amount());
        invoice.setTotal_amount(request.total_amount());
        invoice.setNote(request.note());
        invoice.setStatus(request.status());

        invoice.getItems().clear();

        if (request.items() != null) {
            for (InvoiceItemRequest item : request.items()) {
                var service = services.findById(item.service_id())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Service not found"
                                )
                        );

                var reading = item.reading_id() == null
                        ? null
                        : readings.findById(item.reading_id())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Meter reading not found"
                                )
                        );

                InvoiceItem invoiceItem = InvoiceItem.builder()
                        .invoice(invoice)
                        .service(service)
                        .reading(reading)
                        .description(item.description())
                        .unit(item.unit())
                        .unit_price(item.unit_price())
                        .amount(item.amount())
                        .build();

                invoice.getItems().add(invoiceItem);
            }
        }

        return invoices.save(invoice);
    }

    // -------------------------------------------------------------------------
    // Delete invoice
    // -------------------------------------------------------------------------

    // Xóa hóa đơn theo UUID sau khi xác nhận hóa đơn tồn tại.
    @Override
    @Transactional
    public void delete(UUID id) {
        invoices.delete(get(id));
    }

    // -------------------------------------------------------------------------
    // Response mapping
    // -------------------------------------------------------------------------

    // Chuyển hóa đơn và danh sách chi tiết thành InvoiceResponse.
    // Nếu chi tiết không có chỉ số công tơ liên kết, reading_id trả về null.
    @Override
    public InvoiceResponse response(Invoice value) {
        List<InvoiceItemResponse> items = value.getItems()
                .stream()
                .map(item -> new InvoiceItemResponse(
                        item.getItem_id(),
                        item.getService().getService_id(),
                        item.getReading() == null
                                ? null
                                : item.getReading().getReading_id(),
                        item.getDescription(),
                        item.getUnit(),
                        item.getUnit_price(),
                        item.getAmount()
                ))
                .toList();

        return new InvoiceResponse(
                value.getInvoice_id(),
                value.getContract().getContract_id(),
                value.getInvoice_number(),
                value.getIssued_at(),
                value.getDue_date(),
                value.getSubtotal(),
                value.getDiscount_amount(),
                value.getTax_amount(),
                value.getTotal_amount(),
                value.getNote(),
                value.getStatus(),
                items
        );
    }
}