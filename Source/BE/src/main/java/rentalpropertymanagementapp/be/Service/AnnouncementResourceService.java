package rentalpropertymanagementapp.be.Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.DTO.*;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Announcement.*;
import rentalpropertymanagementapp.be.Model.Enum.*;
import rentalpropertymanagementapp.be.Repository.*;
import java.time.LocalDateTime;
import java.util.*;
@Service public class AnnouncementResourceService {
    private final AnnouncementRepository announcements; private final UserRepository users;
    public AnnouncementResourceService(AnnouncementRepository announcements, UserRepository users) { this.announcements=announcements; this.users=users; }
    public List<Announcement> list(){return announcements.findAll();}
    public Announcement get(UUID id){return announcements.findById(id).orElseThrow(()->new ResourceNotFoundException("Announcement not found"));}
    @Transactional public Announcement save(UUID id, AnnouncementRequest request){Announcement value=id==null?new Announcement():get(id);value.setContent(request.content().trim());value.setAnnouncement_type(request.announcement_type());value.setStatus(request.send()?AnnouncementStatus.SENTED:AnnouncementStatus.NOT_SEND);value.setSent_at(request.send()?(value.getSent_at()==null?LocalDateTime.now():value.getSent_at()):null);value.getUserAnnouncements().clear();for(UUID userId:request.recipient_ids()){var user=users.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found"));value.getUserAnnouncements().add(UserAnnouncement.builder().id(new UserAnnouncementId(value.getAnnouncement_id(),userId)).announcement(value).user(user).status(ReadStatus.NOT_READ).build());}return announcements.save(value);}
    @Transactional public void delete(UUID id){announcements.delete(get(id));}
    public AnnouncementResponse response(Announcement value){return new AnnouncementResponse(value.getAnnouncement_id(),value.getContent(),value.getAnnouncement_type(),value.getStatus(),value.getSent_at(),value.getCreated_at(),value.getUserAnnouncements().stream().map(x->x.getUser().getUser_id()).toList());}
}
