package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import rentalpropertymanagementapp.be.DTO.AnnouncementRequest;
import rentalpropertymanagementapp.be.DTO.AnnouncementResponse;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Announcement.Announcement;
import rentalpropertymanagementapp.be.Model.Announcement.UserAnnouncement;
import rentalpropertymanagementapp.be.Model.Announcement.UserAnnouncementId;
import rentalpropertymanagementapp.be.Model.Enum.AnnouncementStatus;
import rentalpropertymanagementapp.be.Model.Enum.ReadStatus;
import rentalpropertymanagementapp.be.Repository.AnnouncementRepository;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class AnnouncementServiceImplement implements AnnouncementService {

    private final AnnouncementRepository announcements;
    private final UserRepository users;

    // Khởi tạo service và tiêm các Repository cần thiết.
    public AnnouncementServiceImplement(
            AnnouncementRepository announcements,
            UserRepository users
    ) {
        this.announcements = announcements;
        this.users = users;
    }

    // -------------------------------------------------------------------------
    // Announcement retrieval
    // -------------------------------------------------------------------------

    // Lấy danh sách tất cả thông báo.
    @Override
    public List<Announcement> list() {
        return announcements.findAll();
    }

    // Tìm thông báo theo UUID.
    // Nếu không tìm thấy, ném ResourceNotFoundException.
    @Override
    public Announcement get(UUID id) {
        return announcements.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Announcement not found"
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Create and update announcement
    // -------------------------------------------------------------------------

    // Tạo mới hoặc cập nhật thông báo cùng danh sách người nhận.
    // Nếu tạo mới, lưu thông báo trước để lấy announcement_id.
    // Sau đó tạo các bản ghi UserAnnouncement với khóa tổng hợp hợp lệ.
    @Override
    @Transactional
    public Announcement save(
            UUID id,
            AnnouncementRequest request
    ) {
        boolean isNew = id == null;

        Announcement announcement = isNew
                ? new Announcement()
                : get(id);

        announcement.setContent(request.content().trim());
        announcement.setAnnouncement_type(
                request.announcement_type()
        );

        announcement.setStatus(
                request.send()
                        ? AnnouncementStatus.SENTED
                        : AnnouncementStatus.NOT_SEND
        );

        announcement.setSent_at(
                request.send()
                        ? (announcement.getSent_at() == null
                        ? LocalDateTime.now()
                        : announcement.getSent_at())
                        : null
        );

        // Save first so a newly created announcement has its UUID.
        if (isNew) {
            announcement = announcements.save(announcement);
        }

        // Replace the existing recipient assignments.
        announcement.getUserAnnouncements().clear();

        List<UUID> recipientIds = request.recipient_ids();

        if (recipientIds != null) {
            // Avoid creating duplicate assignments for the same recipient.
            for (UUID userId : new LinkedHashSet<>(recipientIds)) {
                if (userId == null) {
                    throw new ResourceNotFoundException(
                            "User not found"
                    );
                }

                var user = users.findById(userId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

                UserAnnouncement userAnnouncement =
                        UserAnnouncement.builder()
                                .id(
                                        new UserAnnouncementId(
                                                announcement.getAnnouncement_id(),
                                                userId
                                        )
                                )
                                .announcement(announcement)
                                .user(user)
                                .status(ReadStatus.NOT_READ)
                                .build();

                announcement.getUserAnnouncements()
                        .add(userAnnouncement);
            }
        }

        return announcements.save(announcement);
    }

    // -------------------------------------------------------------------------
    // Delete announcement
    // -------------------------------------------------------------------------

    // Xóa thông báo sau khi xác nhận thông báo tồn tại.
    @Override
    @Transactional
    public void delete(UUID id) {
        announcements.delete(get(id));
    }

    // -------------------------------------------------------------------------
    // Response mapping
    // -------------------------------------------------------------------------

    // Chuyển entity thông báo thành AnnouncementResponse,
    // bao gồm nội dung, trạng thái, thời gian và danh sách UUID người nhận.
    @Override
    public AnnouncementResponse response(Announcement value) {
        return new AnnouncementResponse(
                value.getAnnouncement_id(),
                value.getContent(),
                value.getAnnouncement_type(),
                value.getStatus(),
                value.getSent_at(),
                value.getCreated_at(),
                value.getUserAnnouncements()
                        .stream()
                        .map(
                                userAnnouncement ->
                                        userAnnouncement.getUser().getUser_id()
                        )
                        .toList()
        );
    }
}