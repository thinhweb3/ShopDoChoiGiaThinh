package com.example.asm.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String uploadRoot = "./data/uploads";

    public String getUploadRoot() {
        return uploadRoot;
    }

    public void setUploadRoot(String uploadRoot) {
        this.uploadRoot = uploadRoot;
    }

    public Path resolveUploadRoot() {
        return Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    public Path resolveFolder(String folder) {
        return resolveUploadRoot().resolve(folder).normalize();
    }
}
