package rentalpropertymanagementapp.be.Service;

import rentalpropertymanagementapp.be.DTO.RentalContractRequest;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RentalContractService {
    List<RentalContract> getAll();
    RentalContract get(UUID id);
    RentalContract create(RentalContractRequest request, MultipartFile document);
    RentalContract update(UUID id, RentalContractRequest request, MultipartFile document);
    void delete(UUID id);
}
