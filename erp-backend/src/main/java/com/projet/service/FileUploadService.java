package com.projet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    private final String uploadDir;

    public FileUploadService() {
        // Use absolute path based on project root
        this.uploadDir = System.getProperty("user.dir")
                + File.separator + "uploads"
                + File.separator + "photos";
    }

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public String uploadPhoto(MultipartFile file) throws IOException {
        // 1. Create upload directory using absolute path
        String baseDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(baseDir, "uploads", "photos");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. Get file extension ONLY — ignore original filename completely
        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg"; // default
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase()
                .replaceAll("[^.a-zA-Z0-9]", ""); // remove special chars
        }

        // 3. Generate ONE simple clean filename — UUID + extension only
        String cleanFilename = UUID.randomUUID().toString() + extension;

        // 4. Save the file
        Path targetPath = uploadPath.resolve(cleanFilename);
        Files.copy(
            file.getInputStream(),
            targetPath,
            StandardCopyOption.REPLACE_EXISTING
        );

        // 5. Log for verification
        System.out.println("=== PHOTO SAVED ===");
        System.out.println("Directory : " + uploadPath.toAbsolutePath());
        System.out.println("Filename  : " + cleanFilename);
        System.out.println("Full path : " + targetPath.toAbsolutePath());
        System.out.println("Exists    : " + Files.exists(targetPath));
        System.out.println("===================");

        // 6. Return clean URL
        return "/uploads/photos/" + cleanFilename;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    public boolean isValidPhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return true; // Optional field
        }

        // Check size
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Check extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String fileExtension = getFileExtension(originalFilename);
        return ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    public String getPhotoUploadDir() {
        return uploadDir;
    }
}
