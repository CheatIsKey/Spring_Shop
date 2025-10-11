package capstone.capstone_shop.config;

import capstone.capstone_shop.service.GcsUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import java.util.UUID;

@Configuration
@Profile("temp") // --spring.profiles.active=temp 에서만 로딩
public class TempStorageConfig {

    // 기본값: OS tmp 밑에 shop-uploads. 원하면 application-temp.yml에서 app.upload.dir로 바꿀 수 있음
    @Value("${app.upload.dir:${java.io.tmpdir}/shop-uploads}")
    private String uploadBaseDir;

    @Bean
    public GcsUploader tempGcsUploader() {
        return new GcsUploader(null) {
            @Override
            public String uploadFile(MultipartFile image) throws IOException {
                // 로컬 고정 경로(예: C:\Users\...\AppData\Local\Temp\shop-uploads\support)
                Path dir = Paths.get(uploadBaseDir, "support");
                Files.createDirectories(dir); // ← 폴더 보장(핵심)

                String original = image.getOriginalFilename();
                String safeName = (original == null ? "file" : original).replace("\\", "_").replace("/", "_");
                String filename = UUID.randomUUID() + "_" + safeName;

                Path dest = dir.resolve(filename);

                try (InputStream in = image.getInputStream()) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }

                // 정적 리소스 핸들러(/files/**)로 접근하게 반환
                return "/files/support/" + filename;
            }
        };
    }
}
