package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserProfileUpdateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.DTO.UserUpdateRequest;
import rentalpropertymanagementapp.be.Exception.TenantHasContractException;
import rentalpropertymanagementapp.be.Exception.UserNotFoundException;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Model.Enum.Gender;
import rentalpropertymanagementapp.be.Model.Enum.IdentityType;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Model.User.Tenant;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.RoleRepository;
import rentalpropertymanagementapp.be.Repository.TenantRepository;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImplement implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Khởi tạo UserServiceImplement và tiêm các dependency cần thiết.
     * Các Repository dùng để truy vấn dữ liệu, PasswordEncoder dùng để
     * mã hóa mật khẩu và kiểm tra mật khẩu khi đăng nhập.
     */
    public UserServiceImplement(
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            ContractTenantRepository contractTenantRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.contractTenantRepository = contractTenantRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy thông tin một người dùng theo UUID.
     * Nếu không tìm thấy người dùng, ném UserNotFoundException.
     * Nếu tìm thấy, chuyển dữ liệu sang UserTenantResponse trước khi trả về.
     */
    @Override
    public UserTenantResponse getUserByID(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return toResponse(user);
    }

    /**
     * Lấy danh sách người dùng theo từ khóa tìm kiếm.
     * Nếu từ khóa null hoặc rỗng thì truy vấn danh sách không có điều kiện tìm kiếm.
     * Kết quả được chuyển từ User sang UserTenantResponse.
     */
    @Override
    public List<UserTenantResponse> getUsers(String search) {
        String term = search == null || search.isBlank()
                ? null
                : search.trim();

        return userRepository.searchWithTenant(term)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Xác thực thông tin đăng nhập.
     * Kiểm tra username, password, loại đăng nhập, trạng thái tài khoản,
     * mật khẩu đã mã hóa và quyền truy cập tương ứng.
     * Trả về Optional chứa User nếu hợp lệ; ngược lại trả về Optional.empty().
     */
    @Override
    public Optional<User> authenticate(
            String username,
            String password,
            LoginType loginType
    ) {
        if (username == null || password == null || loginType == null) {
            return Optional.empty();
        }

        User user = userRepository.findByUser_nameLike(username);

        if (user == null
                || user.getStatus() != ActiveStatus.ACTIVE
                || user.getPassword_hash() == null
                || !passwordEncoder.matches(
                password,
                user.getPassword_hash()
        )
                || !authorize(user, loginType)) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * Kiểm tra người dùng có được phép đăng nhập theo loại tài khoản hay không.
     * LoginType.Manage yêu cầu vai trò LANDLORD.
     * LoginType.Use yêu cầu vai trò TENANT.
     * Tài khoản và vai trò đều phải ở trạng thái ACTIVE.
     */
    @Override
    public Boolean authorize(User user, LoginType loginType) {
        if (user == null
                || loginType == null
                || user.getRole() == null) {
            return false;
        }

        Role role = user.getRole();

        if (role.getStatus() != ActiveStatus.ACTIVE
                || role.getRole_name() == null) {
            return false;
        }

        String requiredRole = switch (loginType) {
            case Manage -> "LANDLORD";
            case Use -> "TENANT";
        };

        return requiredRole.equalsIgnoreCase(role.getRole_name());
    }

    /**
     * Tạo tài khoản người dùng mới với vai trò TENANT.
     * Mật khẩu được mã hóa trước khi lưu vào cơ sở dữ liệu.
     * Đồng thời tạo hồ sơ Tenant và liên kết hồ sơ đó với User.
     * Toàn bộ thao tác được thực hiện trong một transaction.
     */
    @Override
    @Transactional
    public UserTenantResponse createUser(UserCreateRequest request) {
        Role role = roleRepository.findByRoleName("TENANT");

        if (role == null) {
            throw new IllegalStateException(
                    "Role TENANT chưa được cấu hình"
            );
        }

        Tenant tenant = new Tenant();

        User user = User.builder()
                .user_name(request.user_name().trim())
                .password_hash(
                        passwordEncoder.encode(request.password())
                )
                .status(ActiveStatus.ACTIVE)
                .role(role)
                .tenant(tenant)
                .build();

        tenant.setUser(user);

        return toResponse(userRepository.save(user));
    }

    /**
     * Xóa người dùng theo UUID.
     * Nếu người dùng đã có Tenant liên kết với hợp đồng thì không cho xóa.
     * Nếu không vi phạm điều kiện, xóa User, đồng bộ thao tác xuống cơ sở dữ liệu
     * rồi xóa hồ sơ Tenant còn tồn tại.
     * Các thao tác nằm trong cùng một transaction.
     */
    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        Tenant tenant = user.getTenant();

        if (tenant != null
                && contractTenantRepository.existsByTenantId(
                tenant.getTenant_id()
        )) {
            throw new TenantHasContractException();
        }

        userRepository.delete(user);
        userRepository.flush();

        if (tenant != null
                && tenantRepository.existsById(tenant.getTenant_id())) {
            tenantRepository.deleteById(tenant.getTenant_id());
        }
    }

    /**
     * Cập nhật vai trò và trạng thái tài khoản người dùng.
     * Truy vấn tài khoản theo UUID, xác định vai trò mới và chuyển trạng thái
     * từ chuỗi sang ActiveStatus. Nếu vai trò hoặc trạng thái không hợp lệ,
     * phát sinh ngoại lệ. Sau đó lưu thay đổi và trả về dữ liệu đã cập nhật.
     */
    @Override
    @Transactional
    public UserTenantResponse updateUserByID(
            UUID id,
            UserUpdateRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        Role role = roleRepository.findByRoleName(
                request.role().trim().toUpperCase(Locale.ROOT)
        );

        if (role == null) {
            throw new IllegalArgumentException("Role không tồn tại");
        }

        ActiveStatus status;

        try {
            status = ActiveStatus.valueOf(
                    request.status().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ"
            );
        }

        user.setRole(role);
        user.setStatus(status);

        return toResponse(userRepository.save(user));
    }

    /**
     * Cập nhật hồ sơ cá nhân của người thuê.
     * Bao gồm họ tên, ngày sinh, số điện thoại, email, giới tính,
     * giấy tờ tùy thân, địa chỉ, liên hệ khẩn cấp và ghi chú.
     * Các giá trị chuỗi được làm sạch, ngày tháng và enum được chuyển đổi
     * về đúng kiểu dữ liệu trước khi lưu hồ sơ Tenant.
     */
    @Override
    @Transactional
    public UserTenantResponse updateUserProfile(
            UUID id,
            UserProfileUpdateRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        Tenant tenant = user.getTenant();

        if (tenant == null) {
            throw new IllegalStateException(
                    "Tài khoản chưa có hồ sơ người thuê"
            );
        }

        tenant.setFull_name(clean(request.full_name()));
        tenant.setDate_of_birth(parseDate(request.date_of_birth()));
        tenant.setPhone(clean(request.phone()));
        tenant.setEmail(clean(request.email()));
        tenant.setGender(parseEnum(request.gender(), Gender.class));
        tenant.setIdentityType(
                parseEnum(request.identityType(), IdentityType.class)
        );
        tenant.setIdentity_number(clean(request.identity_number()));
        tenant.setIdentity_issued_date(
                parseDate(request.identity_issued_date())
        );
        tenant.setIdentity_issued_place(
                clean(request.identity_issued_place())
        );
        tenant.setPermanent_address(
                clean(request.permanent_address())
        );
        tenant.setEmergency_contact_name(
                clean(request.emergency_contact_name())
        );
        tenant.setEmergency_contact_phone(
                clean(request.emergency_contact_phone())
        );
        tenant.setAdditional_note(
                clean(request.additional_note())
        );

        tenantRepository.save(tenant);

        return toResponse(user);
    }

    /**
     * Chuẩn hóa chuỗi đầu vào.
     * Trả về null nếu giá trị null hoặc chỉ chứa khoảng trắng;
     * nếu không, loại bỏ khoảng trắng ở đầu và cuối chuỗi.
     */
    private String clean(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    /**
     * Chuyển chuỗi ngày theo định dạng ISO yyyy-MM-dd thành LocalDate.
     * Trả về null nếu giá trị đầu vào null hoặc rỗng sau khi làm sạch.
     */
    private LocalDate parseDate(String value) {
        String cleaned = clean(value);

        return cleaned == null
                ? null
                : LocalDate.parse(cleaned);
    }

    /**
     * Chuyển chuỗi thành hằng số của một kiểu enum được chỉ định.
     * Trả về null nếu đầu vào rỗng; nếu không, chuẩn hóa chữ hoa
     * trước khi chuyển đổi. Giá trị không hợp lệ sẽ phát sinh ngoại lệ.
     */
    private <T extends Enum<T>> T parseEnum(
            String value,
            Class<T> enumClass
    ) {
        String cleaned = clean(value);

        return cleaned == null
                ? null
                : Enum.valueOf(
                enumClass,
                cleaned.toUpperCase(Locale.ROOT)
        );
    }

    /**
     * Chuyển đối tượng User thành UserTenantResponse để trả dữ liệu cho API.
     * Kết hợp thông tin tài khoản, vai trò, trạng thái và hồ sơ người thuê.
     * Nếu User chưa có Tenant hoặc chưa có Role thì các trường tương ứng
     * được gán null.
     */
    private UserTenantResponse toResponse(User user) {
        Tenant tenant = user.getTenant();

        return new UserTenantResponse(
                user.getUser_id(),
                user.getUser_name(),
                user.getRole() == null
                        ? null
                        : user.getRole().getRole_name(),
                user.getStatus(),
                tenant == null ? null : tenant.getTenant_id(),
                tenant == null ? null : tenant.getFull_name(),
                tenant == null ? null : tenant.getDate_of_birth(),
                tenant == null ? null : tenant.getPhone(),
                tenant == null ? null : tenant.getEmail(),
                tenant == null ? null : tenant.getGender(),
                tenant == null ? null : tenant.getAvatar_url(),
                tenant == null ? null : tenant.getIdentityType(),
                tenant == null ? null : tenant.getIdentity_number(),
                tenant == null ? null : tenant.getIdentity_issued_date(),
                tenant == null ? null : tenant.getIdentity_issued_place(),
                tenant == null ? null : tenant.getPermanent_address(),
                tenant == null ? null : tenant.getEmergency_contact_name(),
                tenant == null ? null : tenant.getEmergency_contact_phone(),
                tenant == null ? null : tenant.getAdditional_note(),
                tenant == null ? null : tenant.getCreated_at(),
                tenant == null ? null : tenant.getUpdated_at()
        );
    }
}