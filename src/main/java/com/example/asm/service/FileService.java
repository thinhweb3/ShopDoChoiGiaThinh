package com.example.asm.service;

import com.example.asm.config.StorageProperties;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final int PRODUCT_IMAGE_MAX_SIZE = 1200;
    private static final float PRODUCT_IMAGE_QUALITY = 0.82f;

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
            if (StringUtils.hasText(preferredBaseName)) {
                writeOptimizedJpeg(file, saveFile);
            } else {
                Files.copy(file.getInputStream(), saveFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return safeFileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildFileName(String originalFileName, String preferredBaseName) {
        if (StringUtils.hasText(preferredBaseName)) {
            return sanitizeBaseName(preferredBaseName) + ".jpg";
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

    private void writeOptimizedJpeg(MultipartFile file, Path saveFile) throws IOException {
        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
            throw new IOException("Tệp tải lên không phải ảnh hợp lệ.");
        }

        BufferedImage sizedImage = resizeIfNeeded(source);
        BufferedImage rgbImage = new BufferedImage(sizedImage.getWidth(), sizedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            graphics.drawImage(sizedImage, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("Không tìm thấy bộ ghi ảnh JPG.");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(saveFile.toFile())) {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(PRODUCT_IMAGE_QUALITY);
            }
            writer.write(null, new IIOImage(rgbImage, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int maxSide = Math.max(width, height);
        if (maxSide <= PRODUCT_IMAGE_MAX_SIZE) {
            return source;
        }

        double scale = PRODUCT_IMAGE_MAX_SIZE / (double) maxSide;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }
}
