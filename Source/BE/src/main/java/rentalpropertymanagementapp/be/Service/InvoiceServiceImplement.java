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
import rentalpropertymanagementapp.be.Model.Meter.MeterReading;
import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Repository.InvoiceRepository;
import rentalpropertymanagementapp.be.Repository.MeterReadingRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.ServiceRepository;
import rentalpropertymanagementapp.be.Repository.RoomServiceRepository;
import rentalpropertymanagementapp.be.Repository.MeterRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InvoiceServiceImplement implements InvoiceService {

    private final InvoiceRepository invoices;
    private final RentalContractRepository contracts;
    private final ServiceRepository services;
    private final MeterReadingRepository readings;
    private final RoomServiceRepository roomServices;
    private final MeterRepository meters;

    // Khởi tạo service và tiêm các Repository cần thiết.
    public InvoiceServiceImplement(
            InvoiceRepository invoices,
            RentalContractRepository contracts,
            ServiceRepository services,
            MeterReadingRepository readings,
            RoomServiceRepository roomServices,
            MeterRepository meters
    ) {
        this.invoices = invoices;
        this.contracts = contracts;
        this.services = services;
        this.readings = readings;
        this.roomServices = roomServices;
        this.meters = meters;
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

        if (id != null && request.items() == null) {
            invoice.setDue_date(request.due_date());
            invoice.setStatus(request.status());
            return invoices.save(invoice);
        }

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
        invoice.setIssued_at(request.issued_at() == null
                ? invoice.getIssued_at()
                : request.issued_at());
        invoice.setDue_date(request.due_date());
        invoice.setDiscount_amount(valueOrZero(request.discount_amount()));
        invoice.setTax_amount(valueOrZero(request.tax_amount()));
        invoice.setNote(request.note());
        invoice.setStatus(request.status());

        invoice.getItems().clear();

        var rent = services.findByNameIgnoreCase("Rent")
                .orElseGet(() -> services.save(rentalpropertymanagementapp.be.Model.Service.Service.builder()
                        .name("Rent").unit("tháng").calculation_method("FIXED")
                        .default_unit_price(java.math.BigDecimal.ZERO)
                        .room_status(rentalpropertymanagementapp.be.Model.Enum.AvailableStatus.AVAILABLE)
                        .created_at(java.time.LocalDateTime.now())
                        .updated_at(java.time.LocalDateTime.now()).build()));
        addItem(invoice, rent, "Tiền thuê phòng", "tháng", contract.getRent_amount(), contract.getRent_amount(), null);

        for (RoomService assignment : roomServices.findByRoomId(contract.getRoom().getRoom_id())) {
            if (!Boolean.TRUE.equals(assignment.getIs_active())) continue;
            var service = assignment.getService();
            BigDecimal price = valueOrZero(service.getDefault_unit_price());
            if ("METER".equalsIgnoreCase(service.getCalculation_method())) {
                MeterReading reading = latestReading(contract.getRoom().getRoom_id(), service.getName());
                if (reading == null) continue;
                addItem(invoice, service, service.getName(), service.getUnit(), price,
                        BigDecimal.valueOf(reading.getQuantity()).multiply(price), reading);
            } else {
                addItem(invoice, service, service.getName(), service.getUnit(), price, price, null);
            }
        }

        BigDecimal subtotal = invoice.getItems().stream().map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setSubtotal(subtotal);
        invoice.setTotal_amount(subtotal.subtract(invoice.getDiscount_amount()).add(invoice.getTax_amount()));

        return invoices.save(invoice);
    }

    private void addItem(Invoice invoice, rentalpropertymanagementapp.be.Model.Service.Service service,
                         String description, String unit, BigDecimal unitPrice,
                         BigDecimal amount, MeterReading reading) {
        invoice.getItems().add(InvoiceItem.builder().invoice(invoice).service(service).reading(reading)
                .description(description).unit(unit).unit_price(unitPrice).amount(amount).build());
    }

    private MeterReading latestReading(UUID roomId, String serviceName) {
        String normalized = serviceName.toLowerCase(Locale.ROOT);
        String type = normalized.contains("water") || normalized.contains("nước") ? "WATER" : "ELECTRICITY";
        return meters.findByRoomId(roomId).stream()
                .filter(meter -> type.equalsIgnoreCase(meter.getMeter_type()))
                .flatMap(meter -> readings.findByMeterId(meter.getMeter_id()).stream())
                .findFirst().orElse(null);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
