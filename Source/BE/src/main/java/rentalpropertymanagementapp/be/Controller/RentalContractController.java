package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.RentalContractRequest;
import rentalpropertymanagementapp.be.DTO.RentalContractResponse;
import rentalpropertymanagementapp.be.Service.RentalContractService;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/be/contracts")
public class RentalContractController {
    private final RentalContractService service;
    public RentalContractController(RentalContractService service) { this.service = service; }
    @GetMapping public List<RentalContractResponse> list() { return service.getAll().stream().map(RentalContractResponse::from).toList(); }
    @GetMapping("/{id}") public RentalContractResponse get(@PathVariable UUID id) { return RentalContractResponse.from(service.get(id)); }
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RentalContractResponse> create(@Valid @RequestPart("contract") RentalContractRequest request, @RequestPart(value = "document", required = false) MultipartFile document) { return ResponseEntity.status(HttpStatus.CREATED).body(RentalContractResponse.from(service.create(request, document))); }
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public RentalContractResponse update(@PathVariable UUID id, @Valid @RequestPart("contract") RentalContractRequest request, @RequestPart(value = "document", required = false) MultipartFile document) { return RentalContractResponse.from(service.update(id, request, document)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
