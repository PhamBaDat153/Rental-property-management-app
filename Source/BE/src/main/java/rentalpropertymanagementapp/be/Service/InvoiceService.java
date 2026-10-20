package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.InvoiceRequest;
import rentalpropertymanagementapp.be.DTO.InvoiceResponse;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import rentalpropertymanagementapp.be.Model.Invoice.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    // Lấy danh sách hóa đơn theo hợp đồng và trạng thái.
    List<Invoice> list(UUID contractId, InvoiceStatus status);

    // Lấy thông tin hóa đơn theo UUID.
    Invoice get(UUID id);

    // Tạo mới hoặc cập nhật hóa đơn và các chi tiết hóa đơn.
    Invoice save(UUID id, InvoiceRequest request);

    // Xóa hóa đơn theo UUID.
    void delete(UUID id);

    // Chuyển entity hóa đơn thành DTO phản hồi.
    InvoiceResponse response(Invoice value);
}