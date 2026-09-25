package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserRoleUpdateRequest;
import rentalpropertymanagementapp.be.DTO.UserUpdateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.Exception.TenantHasContractException;
import rentalpropertymanagementapp.be.Exception.UserNotFoundException;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Model.User.Tenant;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.RoleRepository;
import rentalpropertymanagementapp.be.Repository.TenantRepository;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserManagementService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, TenantRepository tenantRepository,
                                 ContractTenantRepository contractTenantRepository, RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.contractTenantRepository = contractTenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserTenantResponse> list(String search) {
        String term = search == null || search.isBlank() ? null : search.trim();
        return userRepository.searchWithTenant(term).stream().map(this::toResponse).toList();
    }

    public UserTenantResponse get(UUID id) {
        return toResponse(userRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    @Transactional
    public UserTenantResponse create(UserCreateRequest request) {
        Role role = roleRepository.findByRoleName("TENANT");
        if (role == null) throw new IllegalStateException("Role TENANT chưa được cấu hình");
        Tenant tenant = new Tenant();
        User user = User.builder()
                .user_name(request.user_name().trim())
                .password_hash(passwordEncoder.encode(request.password()))
                .status(ActiveStatus.ACTIVE)
                .role(role)
                .tenant(tenant)
                .build();
        tenant.setUser(user);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Tenant tenant = user.getTenant();
        if (tenant != null && contractTenantRepository.existsByTenantId(tenant.getTenant_id())) {
            throw new TenantHasContractException();
        }
        userRepository.delete(user);
        userRepository.flush();
        if (tenant != null && tenantRepository.existsById(tenant.getTenant_id())) {
            tenantRepository.deleteById(tenant.getTenant_id());
        }
    }

    @Transactional
    public UserTenantResponse updateRole(UUID id, UserRoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Role role = roleRepository.findByRoleName(request.role().trim().toUpperCase());
        if (role == null) throw new IllegalArgumentException("Role không tồn tại");
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserTenantResponse update(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Role role = roleRepository.findByRoleName(request.role().trim().toUpperCase());
        if (role == null) throw new IllegalArgumentException("Role không tồn tại");
        user.setRole(role);
        try {
            user.setStatus(ActiveStatus.valueOf(request.status().trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        return toResponse(userRepository.save(user));
    }

    private UserTenantResponse toResponse(User user) {
        Tenant t = user.getTenant();
        return new UserTenantResponse(user.getUser_id(), user.getUser_name(),
                user.getRole() == null ? null : user.getRole().getRole_name(), user.getStatus(),
                t == null ? null : t.getTenant_id(), t == null ? null : t.getFull_name(),
                t == null ? null : t.getDate_of_birth(), t == null ? null : t.getPhone(),
                t == null ? null : t.getEmail(), t == null ? null : t.getGender(),
                t == null ? null : t.getAvatar_url(), t == null ? null : t.getIdentityType(),
                t == null ? null : t.getIdentity_number(), t == null ? null : t.getIdentity_issued_date(),
                t == null ? null : t.getIdentity_issued_place(), t == null ? null : t.getPermanent_address(),
                t == null ? null : t.getEmergency_contact_name(), t == null ? null : t.getEmergency_contact_phone(),
                t == null ? null : t.getAdditional_note(), t == null ? null : t.getCreated_at(),
                t == null ? null : t.getUpdated_at());
    }
}
