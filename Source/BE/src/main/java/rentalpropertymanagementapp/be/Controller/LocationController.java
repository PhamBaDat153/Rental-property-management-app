package rentalpropertymanagementapp.be.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentalpropertymanagementapp.be.DTO.LocationRequest;
import rentalpropertymanagementapp.be.DTO.LocationResponse;
import rentalpropertymanagementapp.be.Model.Room.Location;
import rentalpropertymanagementapp.be.Service.LocationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/be/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations() {
        return ResponseEntity.ok(locationService.getLocations().stream()
                .map(LocationResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable UUID id) {
        Location location = locationService.getLocationById(id);
        return location == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(LocationResponse.from(location));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LocationResponse> getLocationByCode(@PathVariable String code) {
        Location location = locationService.getLocationByCode(code);
        return location == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(LocationResponse.from(location));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LocationResponse.from(locationService.createLocation(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable UUID id, @Valid @RequestBody LocationRequest request) {
        Location location = locationService.updateLocation(request, id);
        return location == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(LocationResponse.from(location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        if (locationService.getLocationById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }


}
