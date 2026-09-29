package rentalpropertymanagementapp.be.Controller;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Model.Enum.InvoiceStatus;
import rentalpropertymanagementapp.be.Service.InvoiceResourceService;
import java.util.*;
@RestController @RequestMapping("/be/invoices") public class InvoiceController {
    private final InvoiceResourceService service; public InvoiceController(InvoiceResourceService service) { this.service = service; }
    @GetMapping public List<InvoiceResponse> list(@RequestParam(required = false) UUID contractId, @RequestParam(required = false) InvoiceStatus status) { return service.list(contractId, status).stream().map(service::response).toList(); }
    @GetMapping("/{id}") public InvoiceResponse get(@PathVariable UUID id) { return service.response(service.get(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public InvoiceResponse create(@Valid @RequestBody InvoiceRequest request) { return service.response(service.save(null, request)); }
    @PutMapping("/{id}") public InvoiceResponse update(@PathVariable UUID id, @Valid @RequestBody InvoiceRequest request) { return service.response(service.save(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
