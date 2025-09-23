package capstone.capstone_shop.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalImageStorage implements ImageStorage {

    private final Path root;

    public LocalImageStorage(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new IllegalStateException("업로드 디렉터리 생성 실패: " + root, e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = root.resolve(filename);
            file.transferTo(target.toFile());
            // WebConfig에서 /uploads/** -> file:${file.upload-dir}/ 매핑되어 있어야 함
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("로컬 업로드 실패", e);
        }
    }
}
