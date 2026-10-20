package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.User.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Tìm người dùng theo tên đăng nhập.
    @Query("""
            select u
            from User u
            where u.user_name like :username
            """)
    User findByUser_nameLike(
            @Param("username") String username
    );

    // Tìm kiếm người dùng theo họ tên, số điện thoại hoặc số giấy tờ.
    // Kết hợp LEFT JOIN FETCH để tải hồ sơ Tenant cùng với User.
    // Nếu search là null, trả về tất cả người dùng.
    // Không phân biệt chữ hoa và chữ thường khi tìm kiếm.
    @Query("""
            select distinct u
            from User u
            left join fetch u.tenant t
            where :search is null
               or lower(coalesce(t.full_name, ''))
                    like lower(concat('%', :search, '%'))
               or lower(coalesce(t.phone, ''))
                    like lower(concat('%', :search, '%'))
               or lower(coalesce(t.identity_number, ''))
                    like lower(concat('%', :search, '%'))
            """)
    List<User> searchWithTenant(
            @Param("search") String search
    );
}