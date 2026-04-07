package com.example.asm.service;

import com.example.asm.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Autowired
    private StorageProperties storageProperties;

    public String save(MultipartFile file, String folder) {
        return save(file, folder, null);
    }

    public String save(MultipartFile file, String folder, String preferredBaseName) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String safeFileName = buildFileName(file.getOriginalFilename(), preferredBaseName);

            Path dir = storageProperties.resolveFolder(folder);
            Files.createDirectories(dir);

            Path saveFile = dir.resolve(safeFileName);
            Files.copy(file.getInputStream(), saveFile, StandardCopyOption.REPLACE_EXISTING);

            return safeFileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildFileName(String originalFileName, String preferredBaseName) {
        if (StringUtils.hasText(preferredBaseName)) {
            return sanitizeBaseName(preferredBaseName) + extensionOf(originalFileName);
        }

        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8)
                + "-" + sanitizeFileName(originalFileName);
    }

    private String sanitizeFileName(String originalFileName) {
        String cleanedName = StringUtils.cleanPath(originalFileName == null ? "upload.bin" : originalFileName)
                .replace('\\', '/');
        String fileName = StringUtils.getFilename(cleanedName);
        if (!StringUtils.hasText(fileName)) {
            return "upload.bin";
        }

        String asciiName = fileName.replace('đ', 'd').replace('Đ', 'D');
        asciiName = Normalizer.normalize(asciiName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._-]+|[._-]+$", "");

        return StringUtils.hasText(asciiName) ? asciiName : "upload.bin";
    }

    private String sanitizeBaseName(String value) {
        String safeName = sanitizeFileName(value);
        int lastDot = safeName.lastIndexOf('.');
        if (lastDot > 0) {
            safeName = safeName.substring(0, lastDot);
        }
        return StringUtils.hasText(safeName) ? safeName : "upload";
    }

    private String extensionOf(String originalFileName) {
        String safeName = sanitizeFileName(originalFileName);
        int lastDot = safeName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == safeName.length() - 1) {
            return ".bin";
        }
        return safeName.substring(lastDot);
    }
}
