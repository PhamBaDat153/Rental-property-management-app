package rentalpropertymanagementapp.be.Model.Maintenance;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(
        name = "request_image",
        indexes = @Index(name = "idx_request_image_request_id", columnList = "request_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestImage {
    @Id
    @Column(name = "image_id", nullable = false)
    private UUID image_id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "request_id", nullable = false)
    private MaintenanceRequest request;
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String file_url;
    @Column(length = 50)
    private String image_type;

    @PrePersist
    public void prePersist() {
        image_id = Generators.timeBasedEpochGenerator().generate();
    }
}
