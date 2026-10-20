package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.RentalContractRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.InvoiceRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RentalContractServiceImplement implements RentalContractService {

    private final RentalContractRepository repository;
    private final RoomRepository roomRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final InvoiceRepository invoiceRepository;
    private final CloudinaryFileUploader fileUploader;

    /**
     * Khởi tạo service và tiêm các dependency cần thiết.
     * Các Repository xử lý hợp đồng, phòng, người thuê trong hợp đồng
     * và hóa đơn liên quan. CloudinaryFileUploader dùng để tải tài liệu hợp đồng.
     */
    public RentalContractServiceImplement(
            RentalContractRepository repository,
            RoomRepository roomRepository,
            ContractTenantRepository contractTenantRepository,
            InvoiceRepository invoiceRepository,
            CloudinaryFileUploader fileUploader
    ) {
        this.repository = repository;
        this.roomRepository = roomRepository;
        this.contractTenantRepository = contractTenantRepository;
        this.invoiceRepository = invoiceRepository;
        this.fileUploader = fileUploader;
    }

    /**
     * Lấy danh sách tất cả hợp đồng thuê trong hệ thống.
     */
    public List<RentalContract> getAll() {
        return repository.findAll();
    }

    /**
     * Tìm hợp đồng thuê theo UUID.
     * Nếu không tìm thấy hợp đồng, ném ResourceNotFoundException.
     */
    public RentalContract get(UUID id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Contract not found"
                        )
                );
    }

    /**
     * Tạo hợp đồng thuê mới.
     * Kiểm tra ngày bắt đầu và ngày kết thúc, xác nhận phòng tồn tại,
     * chuyển dữ liệu request thành đối tượng RentalContract,
     * tải tài liệu hợp đồng nếu có và lưu hợp đồng vào cơ sở dữ liệu.
     * Thao tác được thực hiện trong một transaction.
     */
    @Transactional
    public RentalContract create(
            RentalContractRequest request,
            MultipartFile document
    ) {
        validateDates(
                request.start_date(),
                request.end_date()
        );

        Room room = roomRepository.findById(request.room_id())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Room not found"
                        )
                );

        RentalContract contract = toEntity(request, room);

        uploadDocument(contract, document);

        return repository.save(contract);
    }

    /**
     * Cập nhật hợp đồng thuê theo UUID.
     * Kiểm tra tính hợp lệ của ngày hợp đồng, xác nhận hợp đồng và phòng
     * đều tồn tại, sau đó cập nhật các thông tin theo request.
     * Nếu có tài liệu mới, tải tài liệu lên và cập nhật đường dẫn.
     * Cuối cùng lưu hợp đồng đã cập nhật trong một transaction.
     */
    @Transactional
    public RentalContract update(
            UUID id,
            RentalContractRequest request,
            MultipartFile document
    ) {
        validateDates(
                request.start_date(),
                request.end_date()
        );

        RentalContract contract = get(id);

        Room room = roomRepository.findById(request.room_id())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Room not found"
                        )
                );

        RentalContract replacement = toEntity(request, room);

        contract.setRoom(room);
        contract.setStart_date(replacement.getStart_date());
        contract.setEnd_date(replacement.getEnd_date());
        contract.setSigned_at(replacement.getSigned_at());

        contract.setRent_amount(replacement.getRent_amount());
        contract.setDeposit_required(replacement.getDeposit_required());

        contract.setBilling_day(replacement.getBilling_day());
        contract.setPayment_due_days(replacement.getPayment_due_days());

        contract.setStatus(replacement.getStatus());
        contract.setTerms(replacement.getTerms());

        contract.setTerminated_at(replacement.getTerminated_at());
        contract.setTermination_reason(
                replacement.getTermination_reason()
        );

        uploadDocument(contract, document);

        return repository.save(contract);
    }

    /**
     * Xóa hợp đồng thuê theo UUID.
     * Kiểm tra hợp đồng tồn tại và xác nhận không có bản ghi người thuê
     * hoặc hóa đơn phụ thuộc vào hợp đồng.
     * Nếu tồn tại dữ liệu phụ thuộc, từ chối xóa và ném ResourceConflictException.
     * Nếu không có dữ liệu phụ thuộc, xóa hợp đồng khỏi cơ sở dữ liệu.
     */
    @Transactional
    public void delete(UUID id) {
        get(id);

        if (contractTenantRepository.existsByContractId(id)
                || invoiceRepository.existsByContractId(id)) {
            throw new ResourceConflictException(
                    "Contract has dependent records"
            );
        }

        repository.deleteById(id);
    }

    /**
     * Chuyển dữ liệu từ RentalContractRequest thành đối tượng RentalContract.
     * Gán phòng và các thông tin về thời hạn hợp đồng, ngày ký,
     * tiền thuê, tiền đặt cọc, lịch thanh toán, trạng thái,
     * điều khoản và thông tin chấm dứt hợp đồng.
     * Đối tượng được tạo chưa được lưu vào cơ sở dữ liệu.
     */
    private RentalContract toEntity(
            RentalContractRequest request,
            Room room
    ) {
        return RentalContract.builder()
                .room(room)
                .start_date(request.start_date())
                .end_date(request.end_date())
                .signed_at(request.signed_at())
                .rent_amount(request.rent_amount())
                .deposit_required(request.deposit_required())
                .billing_day(request.billing_day())
                .payment_due_days(request.payment_due_days())
                .status(request.status())
                .terms(request.terms())
                .document_url(request.document_url())
                .terminated_at(request.terminated_at())
                .termination_reason(request.termination_reason())
                .build();
    }

    /**
     * Kiểm tra ngày kết thúc hợp đồng không được trước ngày bắt đầu.
     * Nếu ngày kết thúc sớm hơn ngày bắt đầu, ném ResourceConflictException.
     */
    private void validateDates(LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) {
            throw new ResourceConflictException(
                    "Contract end date must not precede start date"
            );
        }
    }

    /**
     * Kiểm tra và tải tài liệu hợp đồng lên Cloudinary.
     * Chấp nhận tài liệu PDF, DOC và DOCX dựa trên Content-Type
     * hoặc phần mở rộng của tên tệp.
     * Nếu không có tài liệu mới, giữ nguyên URL tài liệu hiện tại.
     * Nếu tải thành công, cập nhật document_url bằng secure_url từ Cloudinary.
     * Nếu tệp không hợp lệ hoặc quá trình tải thất bại,
     * ném ResourceConflictException.
     */
    private void uploadDocument(
            RentalContract contract,
            MultipartFile document
    ) {
        if (document == null || document.isEmpty()) {
            return;
        }

        String contentType = document.getContentType();

        String name = document.getOriginalFilename() == null
                ? ""
                : document.getOriginalFilename().toLowerCase();

        boolean supported =
                "application/pdf".equals(contentType)
                        || "application/msword".equals(contentType)
                        || "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        .equals(contentType)
                        || name.endsWith(".pdf")
                        || name.endsWith(".doc")
                        || name.endsWith(".docx");

        if (!supported) {
            throw new ResourceConflictException(
                    "Only PDF and Word documents are accepted"
            );
        }

        try {
            Object secureUrl = fileUploader.uploadDocument(
                    document,
                    "rental/contracts"
            ).get("secure_url");

            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new ResourceConflictException(
                        "Contract document upload returned no URL"
                );
            }

            contract.setDocument_url(secureUrl.toString());

        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ResourceConflictException) {
                throw (ResourceConflictException) exception;
            }

            throw new ResourceConflictException(
                    "Contract document upload failed: "
                            + exception.getMessage()
            );
        }
    }
}