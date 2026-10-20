package rentalpropertymanagementapp.be.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rentalpropertymanagementapp.be.Model.Room.Location;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    // Tìm địa điểm theo mã địa điểm.
    // Trả về Optional rỗng nếu không tìm thấy.
    @Query("""
            select l
            from Location l
            where l.location_code = :code
            """)
    Optional<Location> findByCode(
            @Param("code") String code
    );

    // Kiểm tra mã địa điểm đã tồn tại trong hệ thống hay chưa.
    @Query("""
            select count(l) > 0
            from Location l
            where l.location_code = :code
            """)
    boolean existsByCode(
            @Param("code") String code
    );

    // Kiểm tra mã địa điểm đã được sử dụng bởi địa điểm khác hay chưa.
    // Loại trừ địa điểm có UUID được truyền vào, thường dùng khi cập nhật.
    @Query("""
            select count(l) > 0
            from Location l
            where l.location_code = :code
              and l.location_id <> :id
            """)
    boolean existsByCodeAndIdNot(
            @Param("code") String code,
            @Param("id") UUID id
    );
}