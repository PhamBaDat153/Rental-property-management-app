package rentalpropertymanagementapp.be.Model.Announcement;

import jakarta.persistence.*;
import lombok.*;
import rentalpropertymanagementapp.be.Model.Enum.ReadStatus;
import rentalpropertymanagementapp.be.Model.User.User;

@Entity
@Table(name = "user_announcement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnnouncement {
    @EmbeddedId
    private UserAnnouncementId id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("announcement_id")
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @MapsId("user_id")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) status = ReadStatus.NOT_READ;
    }
}
