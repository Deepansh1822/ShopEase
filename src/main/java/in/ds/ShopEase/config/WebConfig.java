package in.ds.ShopEase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map the /img/** URL pattern to the physical directory on disk
        // This ensures newly uploaded files are immediately available without server restart
        String uploadDir = "src/main/resources/static/img/";
        String path = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/img/**")
                .addResourceLocations(path);
    }
}
