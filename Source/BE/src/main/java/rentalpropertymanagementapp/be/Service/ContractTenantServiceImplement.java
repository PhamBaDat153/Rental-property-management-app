package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenant;
import rentalpropertymanagementapp.be.Model.Contract.ContractTenantId;
import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.User.Tenant;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.TenantRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ContractTenantServiceImplement implements ContractTenantService {

    private final ContractTenantRepository repository;
    private final RentalContractRepository contractRepository;
    private final TenantRepository tenantRepository;

    // Khởi tạo service và tiêm các Repository cần thiết.
    public ContractTenantServiceImplement(
            ContractTenantRepository repository,
            RentalContractRepository contractRepository,
            TenantRepository tenantRepository
    ) {
        this.repository = repository;
        this.contractRepository = contractRepository;
        this.tenantRepository = tenantRepository;
    }

    // -------------------------------------------------------------------------
    // Contract tenant retrieval
    // -------------------------------------------------------------------------

    /**
     * Lấy danh sách tất cả các bản ghi liên kết người thuê với hợp đồng.
     */
    @Override
    public List<ContractTenant> getAll() {
        return repository.findAll();
    }

    /**
     * Tìm người thuê được gán vào hợp đồng theo cặp UUID.
     * Nếu bản ghi liên kết không tồn tại, ném ResourceNotFoundException.
     */
    @Override
    public ContractTenant get(UUID contractId, UUID tenantId) {
        ContractTenantId id = new ContractTenantId(
                contractId,
                tenantId
        );

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Contract tenant assignment not found"
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Create contract tenant assignment
    // -------------------------------------------------------------------------

    /**
     * Gán người thuê vào hợp đồng.
     * Kiểm tra ngày chuyển vào và chuyển ra, ngăn tạo liên kết trùng,
     * xác nhận hợp đồng và người thuê tồn tại.
     * Nếu người thuê được chọn làm đại diện, bỏ trạng thái đại diện
     * của những người thuê khác trong cùng hợp đồng trước khi lưu.
     * Toàn bộ thao tác cơ sở dữ liệu được thực hiện trong transaction.
     */
    @Override
    @Transactional
    public ContractTenant create(ContractTenantRequest request) {
        validateDates(
                request.move_in_date(),
                request.move_out_date()
        );

        ContractTenantId id = new ContractTenantId(
                request.contract_id(),
                request.tenant_id()
        );

        if (repository.existsById(id)) {
            throw new ResourceConflictException(
                    "Tenant is already assigned to contract"
            );
        }

        RentalContract contract = contractRepository.findById(
                request.contract_id()
        ).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Contract not found"
                )
        );

        Tenant tenant = tenantRepository.findById(
                request.tenant_id()
        ).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Tenant not found"
                )
        );

        clearRepresentative(
                request.contract_id(),
                request.tenant_id(),
                request.is_representative()
        );

        return repository.save(
                toEntity(request, id, contract, tenant)
        );
    }

    // -------------------------------------------------------------------------
    // Update contract tenant assignment
    // -------------------------------------------------------------------------

    /**
     * Cập nhật thông tin người thuê trong hợp đồng.
     * Không cho phép thay đổi contractId hoặc tenantId của bản ghi hiện tại.
     * Kiểm tra ngày chuyển vào và chuyển ra, cập nhật vai trò đại diện
     * cùng thời gian cư trú, sau đó lưu các thay đổi.
     * Toàn bộ thao tác cơ sở dữ liệu được thực hiện trong transaction.
     */
    @Override
    @Transactional
    public ContractTenant update(
            UUID contractId,
            UUID tenantId,
            ContractTenantRequest request
    ) {
        if (!contractId.equals(request.contract_id())
                || !tenantId.equals(request.tenant_id())) {
            throw new ResourceConflictException(
                    "Assignment identifiers cannot change"
            );
        }

        validateDates(
                request.move_in_date(),
                request.move_out_date()
        );

        ContractTenant assignment = get(contractId, tenantId);

        clearRepresentative(
                contractId,
                tenantId,
                request.is_representative()
        );

        assignment.setIs_representative(
                request.is_representative()
        );
        assignment.setMove_in_date(
                request.move_in_date()
        );
        assignment.setMove_out_date(
                request.move_out_date()
        );

        return repository.save(assignment);
    }

    // -------------------------------------------------------------------------
    // Delete contract tenant assignment
    // -------------------------------------------------------------------------

    /**
     * Xóa liên kết giữa một người thuê và một hợp đồng.
     * Xác nhận bản ghi tồn tại trước khi xóa theo khóa tổng hợp.
     * Thao tác xóa được thực hiện trong transaction.
     */
    @Override
    @Transactional
    public void delete(UUID contractId, UUID tenantId) {
        get(contractId, tenantId);

        ContractTenantId id = new ContractTenantId(
                contractId,
                tenantId
        );

        repository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Chuyển dữ liệu từ ContractTenantRequest thành entity ContractTenant.
     * Liên kết khóa tổng hợp, hợp đồng, người thuê, trạng thái đại diện
     * và thời gian chuyển vào/chuyển ra.
     * Entity được tạo chưa được lưu vào cơ sở dữ liệu.
     */
    private ContractTenant toEntity(
            ContractTenantRequest request,
            ContractTenantId id,
            RentalContract contract,
            Tenant tenant
    ) {
        return ContractTenant.builder()
                .id(id)
                .rentalContract(contract)
                .tenant(tenant)
                .is_representative(request.is_representative())
                .move_in_date(request.move_in_date())
                .move_out_date(request.move_out_date())
                .build();
    }

    /**
     * Kiểm tra ngày chuyển ra không được trước ngày chuyển vào.
     * Nếu cả hai ngày đều có giá trị và ngày chuyển ra sớm hơn,
     * ném ResourceConflictException.
     */
    private void validateDates(LocalDate start, LocalDate end) {
        if (end != null
                && start != null
                && end.isBefore(start)) {
            throw new ResourceConflictException(
                    "Move-out date must not precede move-in date"
            );
        }
    }

    /**
     * Bảo đảm mỗi hợp đồng chỉ có một người thuê được đánh dấu đại diện.
     * Nếu người thuê được chọn không phải đại diện, không thay đổi dữ liệu.
     * Nếu được chọn làm đại diện, bỏ trạng thái đại diện của những người thuê
     * khác trong cùng hợp đồng. Người thuê đang được xử lý được giữ nguyên.
     */
    private void clearRepresentative(
            UUID contractId,
            UUID selectedTenantId,
            Boolean representative
    ) {
        if (!Boolean.TRUE.equals(representative)) {
            return;
        }

        repository.findRepresentativesByContractId(contractId)
                .stream()
                .filter(
                        item -> !item.getId()
                                .getTenant_id()
                                .equals(selectedTenantId)
                )
                .forEach(
                        item -> item.setIs_representative(false)
                );
    }
}