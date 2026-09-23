package rentalpropertymanagementapp.be.Model.Announcement;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.AnnouncementStatus;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "announcement",
        indexes = @Index(name = "idx_announcement_status", columnList = "status")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @Column(name = "announcement_id", nullable = false)
    private UUID announcement_id;

    @Column(nullable = false)
    private String content;

    @Column(name = "announcement_type", length = 50)
    private String announcement_type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementStatus status;

    private LocalDateTime sent_at;
    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "announcement", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, orphanRemoval = true)
    private Set<UserAnnouncement> userAnnouncements = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        announcement_id = Generators.timeBasedEpochGenerator().generate();
        created_at = LocalDateTime.now();
        if (status == null) status = AnnouncementStatus.SENTED;
    }

    @PreUpdate
    public void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}
