package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.LocationRequest;
import rentalpropertymanagementapp.be.Model.Room.Location;
import rentalpropertymanagementapp.be.Repository.LocationRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class LocationServiceImplement implements LocationService {

    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public LocationServiceImplement(LocationRepository locationRepository, RoomRepository roomRepository) {
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
    }
    
    @Override
    public Location getLocationById(UUID id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    @Override
    public Location getLocationByCode(String code) {
        return locationRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    @Override
    public List<Location> getLocations() {
        return locationRepository.findAll();
    }

    @Transactional
    @Override
    public Location createLocation(LocationRequest location) {
        Location newLocation = toEntity(location);
        if (locationRepository.existsByCode(newLocation.getLocation_code())) throw new ResourceConflictException("Location code already exists");
        return locationRepository.save(newLocation);
    }

    @Transactional
    @Override
    public void deleteLocation(UUID id) {
        if (!locationRepository.existsById(id)) throw new ResourceNotFoundException("Location not found");
        if (roomRepository.existsByLocationId(id)) throw new ResourceConflictException("Location has rooms");
        locationRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Location updateLocation(LocationRequest location, UUID id) {
        Location updatedLocation = toEntity(location);
        Location originalLocation = locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        if (locationRepository.existsByCodeAndIdNot(updatedLocation.getLocation_code(), id)) throw new ResourceConflictException("Location code already exists");
            originalLocation.setLocation_code(updatedLocation.getLocation_code());
            originalLocation.setAddress_line(updatedLocation.getAddress_line());
            originalLocation.setWard_name(updatedLocation.getWard_name());
            originalLocation.setDistrict_name(updatedLocation.getDistrict_name());
            originalLocation.setProvince_name(updatedLocation.getProvince_name());
            originalLocation.setDescription(updatedLocation.getDescription());
            return locationRepository.save(originalLocation);
    }

    private Location toEntity(LocationRequest request) {
        return Location.builder()
                .location_code(request.location_code())
                .address_line(request.address_line())
                .ward_name(request.ward_name())
                .district_name(request.district_name())
                .province_name(request.province_name())
                .description(request.description())
                .build();
    }

}
