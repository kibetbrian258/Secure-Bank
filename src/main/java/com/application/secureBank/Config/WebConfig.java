package com.application.secureBank.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:./uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources (like default images)
        registry.addResourceHandler("/api/static/**")
                .addResourceLocations("classpath:/static/");

        // Serve uploaded files directly (alternative to FileController)
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadPathString = uploadPath.toUri().toString();

        // This mapping is optional - we have a dedicated controller for this
        // Keeping it as a backup option if needed
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations(uploadPathString);
    }
}