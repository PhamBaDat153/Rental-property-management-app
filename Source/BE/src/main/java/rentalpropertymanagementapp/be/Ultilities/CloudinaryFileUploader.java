package rentalpropertymanagementapp.be.Ultilities;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryFileUploader {

    private final Cloudinary cloudinary;

    public CloudinaryFileUploader(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadImage(MultipartFile file, String folderName) throws IOException {
        Map options = ObjectUtils.asMap(
                "folder", folderName,
                "resource_type", "image"
        );

        return cloudinary.uploader().upload(file.getBytes(), options);
    }

    public Map uploadDocument(MultipartFile file, String folderName) throws IOException {
        Map options = ObjectUtils.asMap(
                "folder", folderName,
                "resource_type", "auto"
        );

        return cloudinary.uploader().upload(file.getBytes(), options);
    }
}
