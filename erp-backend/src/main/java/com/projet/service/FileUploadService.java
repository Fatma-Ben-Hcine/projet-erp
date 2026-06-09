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
    private static final List<String> DEPOT_ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "zip", "png", "jpg", "jpeg");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final long MAX_DEPOT_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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

    /**
     * DEPRECATED: Use uploadDepotFileHierarchical instead for organized storage
     * Uploads depot file to the old flat structure (kept for backward compatibility)
     */
    public String uploadDepotFile(MultipartFile file) throws IOException {
        String baseDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(baseDir, "uploads", "depots");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase()
                .replaceAll("[^.a-zA-Z0-9]", "");
        }

        String cleanFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadPath.resolve(cleanFilename);
        Files.copy(
            file.getInputStream(),
            targetPath,
            StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println("=== DEPOT FILE SAVED (FLAT) ===");
        System.out.println("Directory : " + uploadPath.toAbsolutePath());
        System.out.println("Filename  : " + cleanFilename);
        System.out.println("Full path : " + targetPath.toAbsolutePath());
        System.out.println("Exists    : " + Files.exists(targetPath));
        System.out.println("================================");

        return targetPath.toAbsolutePath().toString();
    }

    /**
     * NEW: Uploads depot file with hierarchical folder structure
     * Organizes files by project/activity/task for better organization
     * 
     * Folder structure:
     * uploads/depots/projet_{projetId}/activite_{activiteId}/tache_{tacheId}/file.pdf
     * uploads/depots/projet_{projetId}/activite_{activiteId}/file.pdf
     * uploads/depots/projet_{projetId}/file.pdf
     * 
     * @param file The file to upload
     * @param projetId Project ID (required)
     * @param activiteId Activity ID (optional, can be null)
     * @param tacheId Task ID (optional, can be null)
     * @return The absolute path to the saved file
     */
    public String uploadDepotFileHierarchical(MultipartFile file, Long projetId, Long activiteId, Long tacheId) throws IOException {
        String baseDir = System.getProperty("user.dir");
        
        // Build hierarchical path
        StringBuilder pathBuilder = new StringBuilder("uploads" + File.separator + "depots" + File.separator);
        pathBuilder.append("projet_").append(projetId);
        
        if (activiteId != null) {
            pathBuilder.append(File.separator).append("activite_").append(activiteId);
        }
        
        if (tacheId != null) {
            pathBuilder.append(File.separator).append("tache_").append(tacheId);
        }
        
        Path uploadPath = Paths.get(baseDir, pathBuilder.toString());
        
        // Create directory structure if it doesn't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase()
                .replaceAll("[^.a-zA-Z0-9]", "");
        }

        // Generate clean filename with UUID
        String cleanFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadPath.resolve(cleanFilename);
        
        // Copy file to target location
        Files.copy(
            file.getInputStream(),
            targetPath,
            StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println("=== DEPOT FILE SAVED (HIERARCHICAL) ===");
        System.out.println("Projet ID     : " + projetId);
        System.out.println("Activite ID   : " + activiteId);
        System.out.println("Tache ID      : " + tacheId);
        System.out.println("Directory     : " + uploadPath.toAbsolutePath());
        System.out.println("Filename      : " + cleanFilename);
        System.out.println("Full path     : " + targetPath.toAbsolutePath());
        System.out.println("Exists        : " + Files.exists(targetPath));
        System.out.println("=========================================");

        return targetPath.toAbsolutePath().toString();
    }

    public boolean isValidDepotFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > MAX_DEPOT_FILE_SIZE) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String fileExtension = getFileExtension(originalFilename);
        return DEPOT_ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }
}
