package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.asm.config.StorageProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class FileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveShouldWriteToConfiguredFolder() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setUploadRoot(tempDir.toString());

        FileService fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "storageProperties", storageProperties);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile",
                "my image.png",
                "image/png",
                "demo".getBytes());

        String savedFileName = fileService.save(file, "images");

        assertThat(savedFileName).contains("my_image.png");
        assertThat(Files.exists(tempDir.resolve("images").resolve(savedFileName))).isTrue();
    }
}
