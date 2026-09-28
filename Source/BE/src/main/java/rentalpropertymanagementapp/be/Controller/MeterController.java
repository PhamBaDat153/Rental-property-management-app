package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Service.MeterResourceService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/meters")
public class MeterController {
    private final MeterResourceService service;
    public MeterController(MeterResourceService service) { this.service = service; }
    @GetMapping public List<MeterResponse> list(@RequestParam UUID roomId) { return service.listMeters(roomId).stream().map(MeterResponse::from).toList(); }
    @GetMapping("/{id}") public MeterResponse get(@PathVariable UUID id) { return MeterResponse.from(service.getMeter(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MeterResponse create(@Valid @RequestBody MeterRequest request) { return MeterResponse.from(service.createMeter(request)); }
    @PutMapping("/{id}") public MeterResponse update(@PathVariable UUID id, @Valid @RequestBody MeterRequest request) { return MeterResponse.from(service.updateMeter(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.deleteMeter(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/{meterId}/readings") public List<MeterReadingResponse> readings(@PathVariable UUID meterId) { return service.listReadings(meterId).stream().map(MeterReadingResponse::from).toList(); }
}
