package capstone.capstone_shop.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheck())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/users/new", "/css/**", "/js/**", "/images/**", "/webjars/**");

        registry.addInterceptor(new AdminCheck())
                .addPathPatterns("/admin/**");
    }
}
