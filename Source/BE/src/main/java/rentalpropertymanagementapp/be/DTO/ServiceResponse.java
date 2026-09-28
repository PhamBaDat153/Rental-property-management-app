package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Service.Service;
import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(UUID service_id, String name, String unit, String calculation_method,
        BigDecimal default_unit_price, AvailableStatus room_status) {
    public static ServiceResponse from(Service service) {
        return new ServiceResponse(service.getService_id(), service.getName(), service.getUnit(), service.getCalculation_method(),
                service.getDefault_unit_price(), service.getRoom_status());
    }
}
