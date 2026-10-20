package rentalpropertymanagementapp.be.Service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import rentalpropertymanagementapp.be.DTO.RoomRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Exception.ResourceNotFoundException;
import rentalpropertymanagementapp.be.Model.Room.Location;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Model.Room.RoomImage;
import rentalpropertymanagementapp.be.Repository.LocationRepository;
import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImplement implements RoomService {

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;
    private final RentalContractRepository contractRepository;
    private final CloudinaryFileUploader fileUploader;

    /**
     * Khởi tạo service và tiêm các dependency cần thiết.
     * Các Repository xử lý dữ liệu phòng, địa điểm và hợp đồng thuê.
     * CloudinaryFileUploader chịu trách nhiệm tải ảnh lên Cloudinary.
     */
    public RoomServiceImplement(
            RoomRepository roomRepository,
            LocationRepository locationRepository,
            RentalContractRepository contractRepository,
            CloudinaryFileUploader fileUploader
    ) {
        this.roomRepository = roomRepository;
        this.locationRepository = locationRepository;
        this.contractRepository = contractRepository;
        this.fileUploader = fileUploader;
    }

    /**
     * Lấy danh sách tất cả phòng trong hệ thống.
     */
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    /**
     * Tìm phòng theo UUID.
     * Nếu không tìm thấy phòng, ném ResourceNotFoundException.
     */
    public Room get(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Room not found")
                );
    }

    /**
     * Tạo phòng mới theo thông tin nhận được từ RoomRequest.
     * Kiểm tra địa điểm tồn tại và mã phòng không bị trùng trong cùng địa điểm.
     * Lưu thông tin phòng, tải các ảnh đính kèm lên Cloudinary,
     * sau đó lưu lại phòng cùng danh sách ảnh.
     * Toàn bộ thao tác cơ sở dữ liệu nằm trong một transaction.
     */
    @Transactional
    public Room create(RoomRequest request, MultipartFile[] images) {
        Location location = locationRepository.findById(
                request.location_id()
        ).orElseThrow(
                () -> new ResourceNotFoundException("Location not found")
        );

        if (roomRepository.existsByLocationIdAndRoomCode(
                request.location_id(),
                request.room_code()
        )) {
            throw new ResourceConflictException(
                    "Room code already exists in location"
            );
        }

        Room room = roomRepository.save(toEntity(request, location));

        uploadImages(room, images);

        return roomRepository.save(room);
    }

    /**
     * Cập nhật thông tin phòng theo UUID.
     * Kiểm tra phòng và địa điểm tồn tại, đồng thời bảo đảm mã phòng
     * không trùng với phòng khác trong cùng địa điểm.
     * Cập nhật các thuộc tính phòng, tải thêm ảnh được gửi lên
     * rồi lưu thông tin mới vào cơ sở dữ liệu.
     * Thao tác cơ sở dữ liệu nằm trong một transaction.
     */
    @Transactional
    public Room update(
            UUID id,
            RoomRequest request,
            MultipartFile[] images
    ) {
        Room room = get(id);

        Location location = locationRepository.findById(
                request.location_id()
        ).orElseThrow(
                () -> new ResourceNotFoundException("Location not found")
        );

        if (roomRepository.existsByLocationIdAndRoomCodeAndRoomIdNot(
                request.location_id(),
                request.room_code(),
                id
        )) {
            throw new ResourceConflictException(
                    "Room code already exists in location"
            );
        }

        Room replacement = toEntity(request, location);

        room.setLocation(location);
        room.setRoom_code(replacement.getRoom_code());
        room.setRoom_name(replacement.getRoom_name());
        room.setFloor(replacement.getFloor());
        room.setArea_m2(replacement.getArea_m2());
        room.setMax_occupants(replacement.getMax_occupants());
        room.setRent_price(replacement.getRent_price());
        room.setStatus(replacement.getStatus());
        room.setDescription(replacement.getDescription());

        uploadImages(room, images);

        return roomRepository.save(room);
    }

    /**
     * Xóa phòng theo UUID.
     * Kiểm tra phòng tồn tại trước khi xóa.
     * Nếu phòng đang có hợp đồng thuê liên quan thì từ chối xóa
     * và ném ResourceConflictException.
     * Nếu không có hợp đồng liên quan, tiến hành xóa phòng.
     */
    @Transactional
    public void delete(UUID id) {
        get(id);

        if (contractRepository.existsByRoomId(id)) {
            throw new ResourceConflictException(
                    "Room has rental contracts"
            );
        }

        roomRepository.deleteById(id);
    }

    /**
     * Chuyển dữ liệu từ RoomRequest thành đối tượng Room.
     * Gán địa điểm và các thuộc tính như mã phòng, tên phòng,
     * tầng, diện tích, sức chứa, giá thuê, trạng thái và mô tả.
     * Đối tượng tạo ra chưa được lưu vào cơ sở dữ liệu.
     */
    private Room toEntity(RoomRequest request, Location location) {
        return Room.builder()
                .location(location)
                .room_code(request.room_code())
                .room_name(request.room_name())
                .floor(request.floor())
                .area_m2(request.area_m2())
                .max_occupants(request.max_occupants())
                .rent_price(request.rent_price())
                .status(request.status())
                .description(request.description())
                .build();
    }

    /**
     * Tải danh sách ảnh phòng lên Cloudinary và liên kết ảnh với phòng.
     * Bỏ qua khi danh sách ảnh là null.
     * Từ chối tệp rỗng, không có Content-Type hoặc không thuộc loại ảnh.
     * Nếu tải ảnh thất bại do IOException, ném ResourceConflictException.
     */
    private void uploadImages(Room room, MultipartFile[] images) {
        if (images == null) {
            return;
        }

        for (MultipartFile image : images) {
            if (image == null
                    || image.isEmpty()
                    || image.getContentType() == null
                    || !image.getContentType().startsWith("image/")) {
                throw new ResourceConflictException(
                        "Only image files are accepted for room images"
                );
            }

            try {
                String url = String.valueOf(
                        fileUploader.uploadImage(
                                image,
                                "rental/rooms"
                        ).get("secure_url")
                );

                room.getImages().add(
                        RoomImage.builder()
                                .room(room)
                                .image_url(url)
                                .build()
                );

            } catch (IOException exception) {
                throw new ResourceConflictException(
                        "Room image upload failed"
                );
            }
        }
    }
}