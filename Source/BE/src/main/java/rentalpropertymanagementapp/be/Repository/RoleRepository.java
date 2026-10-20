package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.User.Role;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Tìm vai trò theo tên vai trò.
    // Trả về Role nếu tìm thấy, hoặc null nếu không tồn tại.
    @Query("""
            select r
            from Role r
            where r.role_name = :roleName
            """)
    Role findByRoleName(
            @Param("roleName") String roleName
    );
}