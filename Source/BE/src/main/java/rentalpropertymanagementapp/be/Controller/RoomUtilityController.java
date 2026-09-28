package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Service.RoomUtilityService;
import java.util.List;
import java.util.UUID;

@RestController
public class RoomUtilityController {
    private final RoomUtilityService service;
    public RoomUtilityController(RoomUtilityService service) { this.service = service; }
    @GetMapping("/be/services") public List<ServiceResponse> services() { return service.listServices().stream().map(ServiceResponse::from).toList(); }
    @PostMapping("/be/services") @ResponseStatus(HttpStatus.CREATED) public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) { return ServiceResponse.from(service.createService(request)); }
    @PutMapping("/be/services/{id}") public ServiceResponse updateService(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) { return ServiceResponse.from(service.updateService(id, request)); }
    @DeleteMapping("/be/services/{id}") public ResponseEntity<Void> deleteService(@PathVariable UUID id) { service.deleteService(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/be/rooms/{roomId}/services") public List<RoomServiceResponse> roomServices(@PathVariable UUID roomId) { return service.listRoomServices(roomId).stream().map(RoomServiceResponse::from).toList(); }
    @PostMapping("/be/rooms/{roomId}/services/{serviceId}") @ResponseStatus(HttpStatus.CREATED) public RoomServiceResponse assign(@PathVariable UUID roomId, @PathVariable UUID serviceId) { return RoomServiceResponse.from(service.assign(roomId, serviceId)); }
    @PutMapping("/be/rooms/{roomId}/services/{serviceId}") public RoomServiceResponse update(@PathVariable UUID roomId, @PathVariable UUID serviceId, @Valid @RequestBody RoomServiceStatusRequest request) { return RoomServiceResponse.from(service.update(roomId, serviceId, request)); }
    @DeleteMapping("/be/rooms/{roomId}/services/{serviceId}") public ResponseEntity<Void> delete(@PathVariable UUID roomId, @PathVariable UUID serviceId) { service.delete(roomId, serviceId); return ResponseEntity.noContent().build(); }
}
