package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile image information")
public class ProfileImageResponse {
    @Schema(description = "Image URL or Base64 data", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String imageData;
    @Schema(description = "Image content type", example = "Image/jpeg")
    private String contentType;
    @Schema(description = "Last updated timestamp", example = "2023-04-15T10:30:00")
    private String lastUpdated;
}
