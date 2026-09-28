package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.RoomServiceStatusRequest;
import rentalpropertymanagementapp.be.DTO.ServiceRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.Service.RoomService;
import rentalpropertymanagementapp.be.Model.Service.RoomServiceId;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Repository.RoomServiceRepository;
import rentalpropertymanagementapp.be.Repository.ServiceRepository;
import java.util.List;
import java.util.UUID;

@Service
public class RoomUtilityService {
    private final RoomRepository rooms;
    private final ServiceRepository services;
    private final RoomServiceRepository roomServices;

    public RoomUtilityService(RoomRepository rooms, ServiceRepository services, RoomServiceRepository roomServices) {
        this.rooms = rooms; this.services = services; this.roomServices = roomServices;
    }

    public List<rentalpropertymanagementapp.be.Model.Service.Service> listServices() { return services.findAll(); }
    public rentalpropertymanagementapp.be.Model.Service.Service createService(ServiceRequest request) {
        return services.save(rentalpropertymanagementapp.be.Model.Service.Service.builder().name(request.name().trim()).unit(request.unit().trim()).calculation_method(request.calculation_method().trim()).default_unit_price(request.default_unit_price()).room_status(request.room_status()).build());
    }
    public rentalpropertymanagementapp.be.Model.Service.Service updateService(UUID id, ServiceRequest request) {
        var value = services.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        value.setName(request.name().trim()); value.setUnit(request.unit().trim()); value.setCalculation_method(request.calculation_method().trim()); value.setDefault_unit_price(request.default_unit_price()); value.setRoom_status(request.room_status());
        return services.save(value);
    }
    @Transactional public void deleteService(UUID id) { services.delete(services.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"))); }
    public List<RoomService> listRoomServices(UUID roomId) { room(roomId); return roomServices.findByRoomId(roomId); }

    public RoomService assign(UUID roomId, UUID serviceId) {
        Room room = room(roomId); var service = services.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (roomServices.existsAssignment(roomId, serviceId)) throw new ResourceConflictException("Service already assigned");
        return roomServices.save(RoomService.builder().id(new RoomServiceId(roomId, serviceId)).room(room).service(service).is_active(true).build());
    }

    public RoomService update(UUID roomId, UUID serviceId, RoomServiceStatusRequest request) {
        RoomService value = get(roomId, serviceId); value.setIs_active(request.is_active()); return roomServices.save(value);
    }

    @Transactional
    public void delete(UUID roomId, UUID serviceId) { roomServices.delete(get(roomId, serviceId)); }

    private RoomService get(UUID roomId, UUID serviceId) {
        return roomServices.findById(new RoomServiceId(roomId, serviceId)).orElseThrow(() -> new ResourceNotFoundException("Room service not found"));
    }
    private Room room(UUID id) { return rooms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found")); }
}
