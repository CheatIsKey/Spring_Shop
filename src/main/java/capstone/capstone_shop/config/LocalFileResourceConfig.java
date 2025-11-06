package capstone.capstone_shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@Profile("temp")
public class LocalFileResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${java.io.tmpdir}/shop-uploads}")
    private String uploadBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /files/support/xxx -> file:/.../shop-uploads/support/xxx
        String location = Paths.get(uploadBaseDir).toUri().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location.endsWith("/") ? location : (location + "/"));
    }
}
