package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.RoomServiceStatusRequest;
import rentalpropertymanagementapp.be.DTO.ServiceRequest;
import rentalpropertymanagementapp.be.Model.Service.RoomService;

import java.util.List;
import java.util.UUID;

public interface RoomUtilityService {

    List<rentalpropertymanagementapp.be.Model.Service.Service> listServices();

    rentalpropertymanagementapp.be.Model.Service.Service createService(
            ServiceRequest request
    );

    rentalpropertymanagementapp.be.Model.Service.Service updateService(
            UUID id,
            ServiceRequest request
    );

    void deleteService(UUID id);

    List<RoomService> listRoomServices(UUID roomId);

    RoomService assign(UUID roomId, UUID serviceId);

    RoomService update(
            UUID roomId,
            UUID serviceId,
            RoomServiceStatusRequest request
    );

    void delete(UUID roomId, UUID serviceId);
}