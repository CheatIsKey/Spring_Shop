package capstone.capstone_shop.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    String upload(MultipartFile file);
}
