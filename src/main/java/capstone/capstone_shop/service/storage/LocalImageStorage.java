package capstone.capstone_shop.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@ConditionalOnProperty(value = "gcs.enabled", havingValue = "false", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage{

    private final Path root;

    public LocalImageStorage(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir);
        try { Files.createDirectories(root); } catch (Exception ignored) {}
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = root.resolve(filename);
            file.transferTo(target.toFile());
            // 로컬에서 일단 파일 시스템 경로를 URL처럼 반환(뷰에선 img src로 테스트 가능)
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("로컬 업로드 실패", e);
        }
    }
}
