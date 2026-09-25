package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rentalpropertymanagementapp.be.Model.User.User;

import java.util.UUID;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u from User u where u.user_name like :username")
    User findByUser_nameLike(@Param("username") String username);

    @Query("select distinct u from User u left join fetch u.tenant t where " +
            ":search is null or lower(coalesce(t.full_name, '')) like lower(concat('%', :search, '%')) " +
            "or lower(coalesce(t.phone, '')) like lower(concat('%', :search, '%')) " +
            "or lower(coalesce(t.identity_number, '')) like lower(concat('%', :search, '%'))")
    List<User> searchWithTenant(@Param("search") String search);
}
