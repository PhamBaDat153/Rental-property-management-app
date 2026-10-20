package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.MeterReadingRequest;
import rentalpropertymanagementapp.be.DTO.MeterRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Meter.Meter;
import rentalpropertymanagementapp.be.Model.Meter.MeterReading;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Repository.InvoiceItemRepository;
import rentalpropertymanagementapp.be.Repository.MeterReadingRepository;
import rentalpropertymanagementapp.be.Repository.MeterRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class MeterServiceImplement implements MeterService {

    private final MeterRepository meters;
    private final MeterReadingRepository readings;
    private final RoomRepository rooms;
    private final InvoiceItemRepository invoiceItems;
    private final CloudinaryFileUploader fileUploader;

    // Khởi tạo service và tiêm các dependency cần thiết.
    public MeterServiceImplement(
            MeterRepository meters,
            MeterReadingRepository readings,
            RoomRepository rooms,
            InvoiceItemRepository invoiceItems,
            CloudinaryFileUploader fileUploader
    ) {
        this.meters = meters;
        this.readings = readings;
        this.rooms = rooms;
        this.invoiceItems = invoiceItems;
        this.fileUploader = fileUploader;
    }

    // -------------------------------------------------------------------------
    // Meter management
    // -------------------------------------------------------------------------

    // Lấy danh sách công tơ của một phòng.
    // Kiểm tra phòng tồn tại trước khi truy vấn dữ liệu.
    @Override
    public List<Meter> listMeters(UUID roomId) {
        room(roomId);
        return meters.findByRoomId(roomId);
    }

    // Tìm công tơ theo UUID.
    // Nếu không tìm thấy, ném ResourceNotFoundException.
    @Override
    public Meter getMeter(UUID id) {
        return meters.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Meter not found"
                        )
                );
    }

    // Tạo công tơ mới và liên kết với phòng tương ứng.
    @Override
    public Meter createMeter(MeterRequest request) {
        Room room = room(request.room_id());

        Meter meter = Meter.builder()
                .room(room)
                .meter_type(request.meter_type().trim())
                .status(request.status())
                .build();

        return meters.save(meter);
    }

    // Cập nhật phòng liên kết, loại công tơ và trạng thái công tơ.
    @Override
    public Meter updateMeter(UUID id, MeterRequest request) {
        Meter meter = getMeter(id);

        meter.setRoom(room(request.room_id()));
        meter.setMeter_type(request.meter_type().trim());
        meter.setStatus(request.status());

        return meters.save(meter);
    }

    // Xóa công tơ theo UUID.
    // Thao tác được thực hiện trong một transaction.
    @Override
    @Transactional
    public void deleteMeter(UUID id) {
        meters.delete(getMeter(id));
    }

    // -------------------------------------------------------------------------
    // Meter reading management
    // -------------------------------------------------------------------------

    // Lấy danh sách các lần ghi chỉ số của một công tơ.
    // Kiểm tra công tơ tồn tại trước khi truy vấn.
    @Override
    public List<MeterReading> listReadings(UUID meterId) {
        getMeter(meterId);
        return readings.findByMeterId(meterId);
    }

    // Tìm một lần ghi chỉ số theo UUID.
    // Nếu không tìm thấy, ném ResourceNotFoundException.
    @Override
    public MeterReading getReading(UUID id) {
        return readings.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Meter reading not found"
                        )
                );
    }

    // Tạo lần ghi chỉ số mới không kèm ảnh minh chứng.
    @Override
    public MeterReading createReading(
            MeterReadingRequest request
    ) {
        request = withCalculatedValues(request);
        MeterReading reading = toReading(
                new MeterReading(),
                request
        );

        return readings.save(reading);
    }

    // Tạo lần ghi chỉ số mới và tải ảnh minh chứng nếu có.
    @Override
    public MeterReading createReading(
            MeterReadingRequest request,
            MultipartFile evidence
    ) {
        request = withCalculatedValues(request);
        MeterReadingRequest requestWithEvidence =
                withEvidence(request, evidence);

        MeterReading reading = toReading(
                new MeterReading(),
                requestWithEvidence
        );

        return readings.save(reading);
    }

    // Cập nhật thông tin lần ghi chỉ số hiện có.
    @Override
    public MeterReading updateReading(
            UUID id,
            MeterReadingRequest request
    ) {
        MeterReading reading = getReading(id);

        return readings.save(
                toReading(reading, request)
        );
    }

    // Cập nhật thông tin chỉ số và ảnh minh chứng nếu có ảnh mới.
    // Nếu không gửi ảnh mới, giữ nguyên URL được truyền trong request.
    @Override
    public MeterReading updateReading(
            UUID id,
            MeterReadingRequest request,
            MultipartFile evidence
    ) {
        MeterReading reading = getReading(id);

        MeterReadingRequest requestWithEvidence =
                withEvidence(request, evidence);

        return readings.save(
                toReading(reading, requestWithEvidence)
        );
    }

    // Xóa lần ghi chỉ số nếu không được sử dụng trong hóa đơn.
    // Nếu đã được tham chiếu bởi hóa đơn, từ chối xóa.
    @Override
    @Transactional
    public void deleteReading(UUID id) {
        MeterReading reading = getReading(id);

        if (invoiceItems.existsByReadingId(id)) {
            throw new ResourceConflictException(
                    "Meter reading is used for billing"
            );
        }

        readings.delete(reading);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    // Tải ảnh minh chứng lên Cloudinary và tạo request mới chứa URL ảnh.
    // Nếu không có ảnh mới, sử dụng nguyên request ban đầu.
    private MeterReadingRequest withEvidence(
            MeterReadingRequest request,
            MultipartFile evidence
    ) {
        if (evidence == null || evidence.isEmpty()) {
            return request;
        }

        try {
            Object secureUrl = fileUploader.uploadImage(
                    evidence,
                    "rental/meter-readings"
            ).get("secure_url");

            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new ResourceConflictException(
                        "Meter reading evidence upload returned no URL"
                );
            }

            return new MeterReadingRequest(
                    request.meter_id(),
                    request.reading_at(),
                    request.current_value(),
                    request.previous_value(),
                    request.quantity(),
                    secureUrl.toString(),
                    request.note()
            );

        } catch (IOException exception) {
            throw new ResourceConflictException(
                    "Meter reading evidence upload failed"
            );
        }
    }

    // Gán dữ liệu từ request vào đối tượng MeterReading.
    // Đồng thời truy vấn công tơ được tham chiếu để bảo đảm công tơ tồn tại.
    private MeterReading toReading(
            MeterReading reading,
            MeterReadingRequest request
    ) {
        reading.setMeter(getMeter(request.meter_id()));
        reading.setReading_at(request.reading_at());
        reading.setCurrent_value(request.current_value());
        reading.setPrevious_value(request.previous_value());
        reading.setQuantity(request.quantity());
        reading.setEvidence_url(request.evidence_url());
        reading.setNote(request.note());

        return reading;
    }

    private MeterReadingRequest withCalculatedValues(MeterReadingRequest request) {
        if (request.current_value() == null) {
            throw new ResourceConflictException("Current meter value is required");
        }

        List<MeterReading> history = readings.findByMeterId(request.meter_id());
        MeterReading previous = history.isEmpty() ? null : history.get(0);
        BigDecimal previousValue = previous == null
                ? BigDecimal.ZERO
                : previous.getCurrent_value();

        if (request.current_value().compareTo(previousValue) < 0) {
            throw new ResourceConflictException(
                    "Current meter value must not be smaller than previous value"
            );
        }

        return new MeterReadingRequest(
                request.meter_id(),
                request.reading_at(),
                request.current_value(),
                previousValue,
                request.current_value().subtract(previousValue),
                request.evidence_url(),
                request.note()
        );
    }

    // Tìm phòng theo UUID.
    // Nếu phòng không tồn tại, ném ResourceNotFoundException.
    private Room room(UUID id) {
        return rooms.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Room not found"
                        )
                );
    }
}
