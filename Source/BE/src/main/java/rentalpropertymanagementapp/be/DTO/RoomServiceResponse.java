package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Service.RoomService;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoomServiceResponse(UUID room_id, UUID service_id, String name, String unit,
        String calculation_method, java.math.BigDecimal default_unit_price, Boolean is_active,
        LocalDateTime created_at, LocalDateTime updated_at) {
    public static RoomServiceResponse from(RoomService value) {
        var service = value.getService();
        return new RoomServiceResponse(value.getRoom().getRoom_id(), service.getService_id(), service.getName(), service.getUnit(),
                service.getCalculation_method(), service.getDefault_unit_price(), value.getIs_active(), value.getCreated_at(), value.getUpdated_at());
    }
}
