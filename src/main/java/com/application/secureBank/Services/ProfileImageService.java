package com.application.secureBank.Services;

import com.application.secureBank.DTOs.ProfileImageResponse;
import com.application.secureBank.Repositories.ProfileImageRepository;
import com.application.secureBank.models.ProfileImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileImageService {
    private final ProfileImageRepository profileImageRepository;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @Value("${profile.image.default.url:}")
    private String defaultImageUrl;

    @PostConstruct
    public void init() {
        // Initialize storage directory
        fileStorageService.init();
    }

    @Transactional
    public ProfileImageResponse saveProfileImage(String customerId, MultipartFile file) {
        // Validate file
        validateImageFile(file);

        try {
            LocalDateTime now = LocalDateTime.now();

            // Store file in the file system
            String filePath = fileStorageService.storeFile(file, customerId);

            // Check if customer already has a profile image
            Optional<ProfileImage> existingImage = profileImageRepository.findByCustomerId(customerId);

            ProfileImage profileImage;
            if (existingImage.isPresent()) {
                // Update existing image record
                profileImage = existingImage.get();

                // Delete old file if it exists
                if (profileImage.getFilePath() != null && !profileImage.getFilePath().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(profileImage.getFilePath());
                    } catch (Exception e) {
                        log.warn("Could not delete old profile image file: {}", e.getMessage());
                    }
                }

                profileImage.setFilePath(filePath);
                profileImage.setContentType(file.getContentType());
                profileImage.setLastUpdated(now);
                profileImage.setFileSize(file.getSize());
            } else {
                // Create new image record
                profileImage = ProfileImage.builder()
                        .customerId(customerId)
                        .filePath(filePath)
                        .contentType(file.getContentType())
                        .lastUpdated(now)
                        .fileSize(file.getSize())
                        .build();
            }

            // Save the image reference to the database
            ProfileImage savedImage = profileImageRepository.save(profileImage);

            // Return response with URL instead of base64
            return createProfileImageResponse(savedImage);
        } catch (IOException e) {
            log.error("Error processing profile image: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to process the uploaded image");
        }
    }

    public ProfileImageResponse getProfileImage(String customerId) {
        Optional<ProfileImage> profileImageOpt = profileImageRepository.findByCustomerId(customerId);

        if (profileImageOpt.isPresent()) {
            return createProfileImageResponse(profileImageOpt.get());
        } else {
            log.info("No profile image found for customer ID: {}, returning default response", customerId);
            return ProfileImageResponse.builder()
                    .imageData(defaultImageUrl)
                    .contentType("image/png")
                    .lastUpdated(LocalDateTime.now().format(FORMATTER))
                    .build();
        }
    }

    @Transactional
    public ProfileImageResponse deleteProfileImage(String customerId) {
        log.info("Deleting profile image for customer ID: {}", customerId);

        Optional<ProfileImage> profileImageOpt = profileImageRepository.findByCustomerId(customerId);

        if (profileImageOpt.isPresent()) {
            ProfileImage profileImage = profileImageOpt.get();

            // Delete file from file system
            if (profileImage.getFilePath() != null && !profileImage.getFilePath().isEmpty()) {
                try {
                    fileStorageService.deleteFile(profileImage.getFilePath());
                    log.info("Deleted profile image file: {}", profileImage.getFilePath());
                } catch (Exception e) {
                    log.warn("Could not delete profile image file: {}", e.getMessage());
                }
            }

            // Delete record from database
            profileImageRepository.delete(profileImage);

            // Return default image response
            return ProfileImageResponse.builder()
                    .imageData(defaultImageUrl)
                    .contentType("image/png")
                    .lastUpdated(LocalDateTime.now().format(FORMATTER))
                    .build();
        } else {
            log.info("No profile image found to delete for customer ID: {}", customerId);
            return ProfileImageResponse.builder()
                    .imageData(defaultImageUrl)
                    .contentType("image/png")
                    .lastUpdated(LocalDateTime.now().format(FORMATTER))
                    .build();
        }
    }

    private ProfileImageResponse createProfileImageResponse(ProfileImage profileImage) {
        // Get URL for the image instead of encoding to base64
        String imageUrl = fileStorageService.getFileUrl(profileImage.getFilePath());

        return ProfileImageResponse.builder()
                .imageData(imageUrl)
                .contentType(profileImage.getContentType())
                .lastUpdated(profileImage.getLastUpdated().format(FORMATTER))
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check the file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size has exceeded the maximum limit of 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        boolean isValidContentType = false;

        if (contentType != null) {
            for (String allowedType : ALLOWED_CONTENT_TYPES) {
                if (contentType.equals(allowedType)) {
                    isValidContentType = true;
                    break;
                }
            }
        }

        if (!isValidContentType) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }
}