package com.application.secureBank.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.dir:./uploads/images}")
    private String uploadDir;

    @Value("${file.base.url:http://localhost:8080/api/files}")
    private String baseUrl;

    /**
     * Initializes the upload directory
     */
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            log.info("Created file upload directory: {}", uploadDir);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores a file in the file system
     * @param file File to store
     * @param customerId ID of the customer who owns the file
     * @return The relative path to the file
     */
    public String storeFile(MultipartFile file, String customerId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Get original filename and extract extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and customer ID
        String filename = customerId + "_" + UUID.randomUUID() + extension;

        // Create customer directory if it doesn't exist
        Path customerDir = Paths.get(uploadDir, customerId);
        if (!Files.exists(customerDir)) {
            Files.createDirectories(customerDir);
        }

        // Create full path
        Path filePath = customerDir.resolve(filename);

        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Stored file {} for customer {}", filename, customerId);

        // Return relative path (customer ID + filename)
        return customerId + "/" + filename;
    }

    /**
     * Deletes a file from the file system
     * @param filePath Path to the file
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * Retrieves a file from the file system
     * @param filePath Path to the file
     * @return The file content as byte array
     */
    public byte[] getFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        if (!Files.exists(path)) {
            log.error("File not found: {}", filePath);
            throw new RuntimeException("File not found");
        }
        return Files.readAllBytes(path);
    }

    /**
     * Gets the complete URL for a file
     * @param filePath The relative file path
     * @return Full URL to access the file
     */
    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return baseUrl + "/" + filePath;
    }
}