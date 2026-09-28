package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.MeterReadingRequest;
import rentalpropertymanagementapp.be.DTO.MeterReadingResponse;
import rentalpropertymanagementapp.be.Service.MeterResourceService;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/be/meter-readings")
public class MeterReadingController {
    private final MeterResourceService service;
    public MeterReadingController(MeterResourceService service) { this.service = service; }
    @GetMapping("/{id}") public MeterReadingResponse get(@PathVariable UUID id) { return MeterReadingResponse.from(service.getReading(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MeterReadingResponse create(@Valid @RequestBody MeterReadingRequest request) { return MeterReadingResponse.from(service.createReading(request)); }
    @PostMapping(consumes = "multipart/form-data") @ResponseStatus(HttpStatus.CREATED)
    public MeterReadingResponse createMultipart(@Valid @RequestPart("reading") MeterReadingRequest request,
                                                 @RequestPart(value = "evidence", required = false) MultipartFile evidence) {
        return MeterReadingResponse.from(service.createReading(request, evidence));
    }
    @PutMapping("/{id}") public MeterReadingResponse update(@PathVariable UUID id, @Valid @RequestBody MeterReadingRequest request) { return MeterReadingResponse.from(service.updateReading(id, request)); }
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public MeterReadingResponse updateMultipart(@PathVariable UUID id, @Valid @RequestPart("reading") MeterReadingRequest request,
                                                @RequestPart(value = "evidence", required = false) MultipartFile evidence) {
        return MeterReadingResponse.from(service.updateReading(id, request, evidence));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.deleteReading(id); return ResponseEntity.noContent().build(); }
}
