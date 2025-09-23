package capstone.capstone_shop.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
public class GcsConfig {

    /**
     * Cloud Run/GCE 등에서 ADC(기본 자격증명)로 GCS 클라이언트 생성.
     * app.storage=gcs 일 때만 생성되도록 조건을 건다.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "gcs")
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    /**
     * PUT/DELETE 폼 메서드 오버라이드용 필터는 항상 등록(프로파일 무관)
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
