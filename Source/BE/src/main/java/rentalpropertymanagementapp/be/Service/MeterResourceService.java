package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader;

@Service
public class MeterResourceService {
    private final MeterRepository meters;
    private final MeterReadingRepository readings;
    private final RoomRepository rooms;
    private final InvoiceItemRepository invoiceItems;
    private final CloudinaryFileUploader fileUploader;

    public MeterResourceService(MeterRepository meters, MeterReadingRepository readings, RoomRepository rooms,
                                InvoiceItemRepository invoiceItems, CloudinaryFileUploader fileUploader) {
        this.meters = meters; this.readings = readings; this.rooms = rooms; this.invoiceItems = invoiceItems; this.fileUploader = fileUploader;
    }

    public List<Meter> listMeters(UUID roomId) { room(roomId); return meters.findByRoomId(roomId); }
    public Meter getMeter(UUID id) { return meters.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter not found")); }

    public Meter createMeter(MeterRequest request) {
        Room room = room(request.room_id());
        return meters.save(Meter.builder().room(room).meter_type(request.meter_type().trim()).status(request.status()).build());
    }

    public Meter updateMeter(UUID id, MeterRequest request) {
        Meter meter = getMeter(id); meter.setRoom(room(request.room_id())); meter.setMeter_type(request.meter_type().trim()); meter.setStatus(request.status());
        return meters.save(meter);
    }

    @Transactional
    public void deleteMeter(UUID id) { meters.delete(getMeter(id)); }

    public List<MeterReading> listReadings(UUID meterId) { getMeter(meterId); return readings.findByMeterId(meterId); }
    public MeterReading getReading(UUID id) { return readings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter reading not found")); }

    public MeterReading createReading(MeterReadingRequest request) {
        return readings.save(toReading(new MeterReading(), request));
    }
    public MeterReading createReading(MeterReadingRequest request, MultipartFile evidence) {
        return readings.save(toReading(new MeterReading(), withEvidence(request, evidence)));
    }

    public MeterReading updateReading(UUID id, MeterReadingRequest request) {
        return readings.save(toReading(getReading(id), request));
    }
    public MeterReading updateReading(UUID id, MeterReadingRequest request, MultipartFile evidence) {
        return readings.save(toReading(getReading(id), withEvidence(request, evidence)));
    }

    private MeterReadingRequest withEvidence(MeterReadingRequest request, MultipartFile evidence) {
        if (evidence == null || evidence.isEmpty()) return request;
        try {
            String url = String.valueOf(fileUploader.uploadImage(evidence, "rental/meter-readings").get("secure_url"));
            return new MeterReadingRequest(request.meter_id(), request.reading_at(), request.current_value(), request.previous_value(), request.quantity(), url, request.note());
        } catch (IOException exception) {
            throw new ResourceConflictException("Meter reading evidence upload failed");
        }
    }

    @Transactional
    public void deleteReading(UUID id) {
        MeterReading reading = getReading(id);
        if (invoiceItems.existsByReadingId(id)) throw new ResourceConflictException("Meter reading is used for billing");
        readings.delete(reading);
    }

    private MeterReading toReading(MeterReading reading, MeterReadingRequest request) {
        reading.setMeter(getMeter(request.meter_id())); reading.setReading_at(request.reading_at()); reading.setCurrent_value(request.current_value());
        reading.setPrevious_value(request.previous_value()); reading.setQuantity(request.quantity()); reading.setEvidence_url(request.evidence_url()); reading.setNote(request.note());
        return reading;
    }

    private Room room(UUID id) { return rooms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found")); }
}
