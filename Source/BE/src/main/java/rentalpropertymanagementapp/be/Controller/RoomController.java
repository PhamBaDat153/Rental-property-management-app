package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.RoomRequest;
import rentalpropertymanagementapp.be.DTO.RoomResponse;
import rentalpropertymanagementapp.be.Service.RoomService;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/be/rooms")
public class RoomController {
    private final RoomService service;
    public RoomController(RoomService service) { this.service = service; }
    @GetMapping public List<RoomResponse> list() { return service.getAll().stream().map(RoomResponse::from).toList(); }
    @GetMapping("/{id}") public RoomResponse get(@PathVariable UUID id) { return RoomResponse.from(service.get(id)); }
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RoomResponse> create(@Valid @RequestPart("room") RoomRequest request, @RequestPart(value = "images", required = false) MultipartFile[] images) { return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.from(service.create(request, images))); }
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public RoomResponse update(@PathVariable UUID id, @Valid @RequestPart("room") RoomRequest request, @RequestPart(value = "images", required = false) MultipartFile[] images) { return RoomResponse.from(service.update(id, request, images)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
