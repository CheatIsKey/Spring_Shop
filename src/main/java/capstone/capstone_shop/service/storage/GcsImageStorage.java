package capstone.capstone_shop.service.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@ConditionalOnProperty(value = "gcs.enabled", havingValue = "true")
public class GcsImageStorage implements ImageStorage{

    private final Storage storage;
    private final String bucket;

    public GcsImageStorage(
            Storage storage,
            @Value("${spring.cloud.gcp.storage.bucket}") String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String objectName = "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, objectName))
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());
            // 퍼블릭 접근/서명 URL 정책에 따라 URL 생성 방식 조정
            return String.format("https://storage.googleapis.com/%s/%s", bucket, objectName);
        } catch (Exception e) {
            throw new RuntimeException("GCS 업로드 실패", e);
        }
    }
}
