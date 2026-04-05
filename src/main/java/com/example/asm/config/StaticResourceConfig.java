package com.example.asm.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    public StaticResourceConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path imagesPath = storageProperties.resolveFolder("images");
        registry.addResourceHandler("/images/**")
                .addResourceLocations(toResourceLocation(imagesPath), "classpath:/static/images/");
    }

    private String toResourceLocation(Path path) {
        String location = path.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
