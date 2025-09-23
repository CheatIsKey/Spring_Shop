package capstone.capstone_shop.service.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "gcs")
@RequiredArgsConstructor
public class GcsImageStorage implements ImageStorage {

    private final Storage storage;

    /**
     * application(-cloud).yml의 gcs.bucket 값을 사용
     * (우리가 통일한 키: gcs.bucket)
     */
    @Value("${gcs.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {
        try {
            String objectName = "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, objectName))
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // 공개 버킷이라면 아래 URL로 바로 접근 가능
            return "https://storage.googleapis.com/" + bucket + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("GCS 업로드 실패", e);
        }
    }
}
