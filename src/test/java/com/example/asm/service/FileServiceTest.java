package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.asm.config.StorageProperties;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
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

    @Test
    void saveShouldNormalizeVietnameseFileName() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setUploadRoot(tempDir.toString());

        FileService fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "storageProperties", storageProperties);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile",
                "thêm_mấy background_202604072309.png",
                "image/png",
                "demo".getBytes());

        String savedFileName = fileService.save(file, "images");

        assertThat(savedFileName).contains("them_may_background_202604072309.png");
        assertThat(Files.exists(tempDir.resolve("images").resolve(savedFileName))).isTrue();
    }

    @Test
    void saveShouldUsePreferredBaseNameWithOriginalExtension() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setUploadRoot(tempDir.toString());

        FileService fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "storageProperties", storageProperties);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile",
                "thêm_mấy background_202604072309.png",
                "image/png",
                sampleImage("png"));

        String savedFileName = fileService.save(file, "images", "MH001");

        assertThat(savedFileName).isEqualTo("MH001.jpg");
        assertThat(Files.exists(tempDir.resolve("images").resolve(savedFileName))).isTrue();
        assertThat(ImageIO.read(tempDir.resolve("images").resolve(savedFileName).toFile())).isNotNull();
    }

    private byte[] sampleImage(String format) throws Exception {
        BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        try {
            graphics.setColor(Color.ORANGE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }
}
