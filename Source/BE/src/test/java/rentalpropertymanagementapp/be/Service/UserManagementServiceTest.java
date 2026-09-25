package rentalpropertymanagementapp.be.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rentalpropertymanagementapp.be.DTO.UserCreateRequest;
import rentalpropertymanagementapp.be.DTO.UserTenantResponse;
import rentalpropertymanagementapp.be.Exception.TenantHasContractException;
import rentalpropertymanagementapp.be.Model.User.Role;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Repository.ContractTenantRepository;
import rentalpropertymanagementapp.be.Repository.RoleRepository;
import rentalpropertymanagementapp.be.Repository.TenantRepository;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {
    @Mock UserRepository userRepository;
    @Mock TenantRepository tenantRepository;
    @Mock ContractTenantRepository contractTenantRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;

    private UserManagementService service;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userRepository, tenantRepository,
                contractTenantRepository, roleRepository, passwordEncoder);
    }

    @Test
    void createInitializesUserWithEmptyTenant() {
        Role role = Role.builder().role_name("TENANT").build();
        when(roleRepository.findByRoleName("TENANT")).thenReturn(role);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTenantResponse response = service.create(new UserCreateRequest(" tenant ", "secret"));

        verify(userRepository).save(any(User.class));
        assertNull(response.full_name());
        assertNull(response.phone());
        assertNull(response.identity_number());
    }

    @Test
    void listPassesSearchAndMapsTenantFields() {
        User user = User.builder().user_name("tenant-user").build();
        when(userRepository.searchWithTenant("0123")).thenReturn(List.of(user));

        List<UserTenantResponse> result = service.list(" 0123 ");

        verify(userRepository).searchWithTenant("0123");
        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
    }

    @Test
    void deleteRejectsTenantReferencedByContract() {
        UUID tenantId = UUID.randomUUID();
        rentalpropertymanagementapp.be.Model.User.Tenant tenant = new rentalpropertymanagementapp.be.Model.User.Tenant();
        tenant.setTenant_id(tenantId);
        User user = User.builder().tenant(tenant).build();
        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(user));
        when(contractTenantRepository.existsByTenantId(tenantId)).thenReturn(true);

        assertThrows(TenantHasContractException.class, () -> service.delete(UUID.randomUUID()));
        verify(userRepository, never()).delete(any(User.class));
    }
}
