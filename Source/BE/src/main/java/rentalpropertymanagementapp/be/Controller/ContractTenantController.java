package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.DTO.ContractTenantResponse;
import rentalpropertymanagementapp.be.Service.ContractTenantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/contract-tenants")
public class ContractTenantController {
    private final ContractTenantService service;
    public ContractTenantController(ContractTenantService service) { this.service = service; }
    @GetMapping public List<ContractTenantResponse> list() { return service.getAll().stream().map(ContractTenantResponse::from).toList(); }
    @GetMapping("/{contractId}/{tenantId}") public ContractTenantResponse get(@PathVariable UUID contractId, @PathVariable UUID tenantId) { return ContractTenantResponse.from(service.get(contractId, tenantId)); }
    @PostMapping public ResponseEntity<ContractTenantResponse> create(@Valid @RequestBody ContractTenantRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(ContractTenantResponse.from(service.create(request))); }
    @PutMapping("/{contractId}/{tenantId}") public ContractTenantResponse update(@PathVariable UUID contractId, @PathVariable UUID tenantId, @Valid @RequestBody ContractTenantRequest request) { return ContractTenantResponse.from(service.update(contractId, tenantId, request)); }
    @DeleteMapping("/{contractId}/{tenantId}") public ResponseEntity<Void> delete(@PathVariable UUID contractId, @PathVariable UUID tenantId) { service.delete(contractId, tenantId); return ResponseEntity.noContent().build(); }
}
