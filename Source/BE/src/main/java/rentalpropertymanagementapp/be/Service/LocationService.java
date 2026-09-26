package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.LocationRequest;
import rentalpropertymanagementapp.be.Model.Room.Location;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    Location getLocationById(UUID id);
    Location getLocationByCode(String code);
    List<Location> getLocations();
    Location createLocation(LocationRequest location);
    void deleteLocation(UUID id);
    Location updateLocation(LocationRequest location, UUID id);
}
