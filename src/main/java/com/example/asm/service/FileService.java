package com.example.asm.service;

import com.example.asm.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                    ? "upload.bin"
                    : file.getOriginalFilename());
            String safeFileName = Path.of(originalFileName).getFileName().toString().replace(" ", "_");
            if (!StringUtils.hasText(safeFileName)) {
                safeFileName = "upload.bin";
            }

            Path dir = storageProperties.resolveFolder(folder);
            Files.createDirectories(dir);

            String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8)
                    + "-" + safeFileName;
            Path saveFile = dir.resolve(fileName);
            file.transferTo(saveFile);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
