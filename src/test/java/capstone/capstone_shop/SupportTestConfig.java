package capstone.capstone_shop;

import capstone.capstone_shop.service.GcsUploader;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@TestConfiguration
public class SupportTestConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

    @Bean
    @Primary
    public GcsUploader testGcsUploader() {
        return new GcsUploader(null) {
            @Override
            public String uploadFile(MultipartFile image) throws IOException {
                return "https://test-bucket/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
            }
        };
    }
}
