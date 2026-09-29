package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;
import rentalpropertymanagementapp.be.Model.Invoice.InvoiceItem;
import rentalpropertymanagementapp.be.Repository.*;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceResourceService {
    private final InvoiceRepository invoices; private final RentalContractRepository contracts; private final ServiceRepository services; private final MeterReadingRepository readings;
    public InvoiceResourceService(InvoiceRepository invoices, RentalContractRepository contracts, ServiceRepository services, MeterReadingRepository readings) { this.invoices = invoices; this.contracts = contracts; this.services = services; this.readings = readings; }
    public List<Invoice> list(UUID contractId, rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus status) { return invoices.search(contractId, status); }
    public Invoice get(UUID id) { return invoices.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found")); }
    @Transactional public Invoice save(UUID id, InvoiceRequest request) { Invoice invoice = id == null ? new Invoice() : get(id); if (id == null && invoices.existsByInvoiceNumber(request.invoice_number().trim())) throw new ResourceConflictException("Invoice number already exists"); RentalContract contract = contracts.findById(request.contract_id()).orElseThrow(() -> new ResourceNotFoundException("Contract not found")); invoice.setContract(contract); invoice.setInvoice_number(request.invoice_number().trim()); invoice.setIssued_at(request.issued_at()); invoice.setDue_date(request.due_date()); invoice.setSubtotal(request.subtotal()); invoice.setDiscount_amount(request.discount_amount()); invoice.setTax_amount(request.tax_amount()); invoice.setTotal_amount(request.total_amount()); invoice.setNote(request.note()); invoice.setStatus(request.status()); invoice.getItems().clear(); if (request.items() != null) for (InvoiceItemRequest item : request.items()) { var service = services.findById(item.service_id()).orElseThrow(() -> new ResourceNotFoundException("Service not found")); var reading = item.reading_id() == null ? null : readings.findById(item.reading_id()).orElseThrow(() -> new ResourceNotFoundException("Meter reading not found")); invoice.getItems().add(InvoiceItem.builder().invoice(invoice).service(service).reading(reading).description(item.description()).unit(item.unit()).unit_price(item.unit_price()).amount(item.amount()).build()); } return invoices.save(invoice); }
    @Transactional public void delete(UUID id) { invoices.delete(get(id)); }
    public InvoiceResponse response(Invoice value) { return new InvoiceResponse(value.getInvoice_id(), value.getContract().getContract_id(), value.getInvoice_number(), value.getIssued_at(), value.getDue_date(), value.getSubtotal(), value.getDiscount_amount(), value.getTax_amount(), value.getTotal_amount(), value.getNote(), value.getStatus(), value.getItems().stream().map(i -> new InvoiceItemResponse(i.getItem_id(), i.getService().getService_id(), i.getReading() == null ? null : i.getReading().getReading_id(), i.getDescription(), i.getUnit(), i.getUnit_price(), i.getAmount())).toList()); }
}
