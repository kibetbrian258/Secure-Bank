package com.application.secureBank.Controllers;

import com.application.secureBank.DTOs.ProfileImageResponse;
import com.application.secureBank.Services.ProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:4200", "https://secure-bank-sb.vercel.app"})
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Profile", description = "API endpoints for profile management")
@Slf4j
public class ProfileImageController {
    private final ProfileImageService profileImageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload profile image",
            description = "Uploads a new profile image for the authenticated customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid image file",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        log.info("Processing profile image upload for customer ID: {}", customerId);

        try {
            ProfileImageResponse response = profileImageService.saveProfileImage(customerId, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error uploading profile image: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/image")
    @Operation(
            summary = "Get profile image",
            description = "Retrieves the profile image for the authenticated customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<ProfileImageResponse> getProfileImage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        log.info("Retrieving profile image for customer ID: {}", customerId);

        try {
            ProfileImageResponse response = profileImageService.getProfileImage(customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving profile image: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/image")
    @Operation(
            summary = "Delete profile image",
            description = "Deletes the profile image for the authenticated customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<ProfileImageResponse> deleteProfileImage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        log.info("Deleting profile image for customer ID: {}", customerId);

        ProfileImageResponse response = profileImageService.deleteProfileImage(customerId);
        return ResponseEntity.ok(response);
    }
}