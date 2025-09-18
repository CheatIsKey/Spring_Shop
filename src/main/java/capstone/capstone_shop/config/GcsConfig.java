package capstone.capstone_shop.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
@ConditionalOnProperty(value = "gcs.enabled", havingValue = "true")
public class GcsConfig {

    @Bean
    public Storage googleStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
