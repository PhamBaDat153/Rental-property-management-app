package rentalpropertymanagementapp.be.Controller;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.MaintenanceResponse;
import rentalpropertymanagementapp.be.Model.Enum.MaintenanceStatus;
import rentalpropertymanagementapp.be.Service.MaintenanceResourceService;
import java.util.*;
@RestController @RequestMapping("/be/maintenance") public class MaintenanceController {
    private final MaintenanceResourceService service; public MaintenanceController(MaintenanceResourceService service) { this.service = service; }
    @GetMapping public List<MaintenanceResponse> list(@RequestParam(required = false) UUID roomId, @RequestParam(required = false) MaintenanceStatus status, @RequestParam(required = false) String priority) { return service.list(roomId, status, priority).stream().map(service::response).toList(); }
    @GetMapping("/{id}") public MaintenanceResponse get(@PathVariable UUID id) { return service.response(service.get(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MaintenanceResponse create(@Valid @RequestBody rentalpropertymanagementapp.be.DTO.MaintenanceRequest request) { return service.response(service.save(null, request)); }
    @PutMapping("/{id}") public MaintenanceResponse update(@PathVariable UUID id, @Valid @RequestBody rentalpropertymanagementapp.be.DTO.MaintenanceRequest request) { return service.response(service.save(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
