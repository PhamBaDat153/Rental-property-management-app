package rentalpropertymanagementapp.be.Service;

import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.MeterReadingRequest;
import rentalpropertymanagementapp.be.DTO.MeterRequest;
import rentalpropertymanagementapp.be.Model.Meter.Meter;
import rentalpropertymanagementapp.be.Model.Meter.MeterReading;

import java.util.List;
import java.util.UUID;
public interface MeterService {
    // Lấy danh sách công tơ thuộc một phòng.
    List<Meter> listMeters(UUID roomId);

    // Lấy thông tin công tơ theo UUID.
    Meter getMeter(UUID id);

    // Tạo công tơ mới.
    Meter createMeter(MeterRequest request);

    // Cập nhật thông tin công tơ.
    Meter updateMeter(UUID id, MeterRequest request);

    // Xóa công tơ theo UUID.
    void deleteMeter(UUID id);

    // Lấy danh sách chỉ số của một công tơ.
    List<MeterReading> listReadings(UUID meterId);

    // Lấy thông tin một lần ghi chỉ số.
    MeterReading getReading(UUID id);

    // Tạo chỉ số công tơ không kèm ảnh minh chứng.
    MeterReading createReading(MeterReadingRequest request);

    // Tạo chỉ số công tơ kèm ảnh minh chứng.
    MeterReading createReading(
            MeterReadingRequest request,
            MultipartFile evidence
    );

    // Cập nhật chỉ số công tơ không thay đổi ảnh minh chứng.
    MeterReading updateReading(
            UUID id,
            MeterReadingRequest request
    );

    // Cập nhật chỉ số công tơ và ảnh minh chứng nếu được gửi lên.
    MeterReading updateReading(
            UUID id,
            MeterReadingRequest request,
            MultipartFile evidence
    );

    // Xóa lần ghi chỉ số nếu không được sử dụng để lập hóa đơn.
    void deleteReading(UUID id);
}
