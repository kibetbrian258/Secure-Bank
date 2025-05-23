package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer profile information")
public class CustomerProfileResponse {
    @Schema(description = "Customer ID", example = "RD4090")
    private String customerId;

    @Schema(description = "Customer's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Customer's address", example = "123 Main St, City")
    private String address;

    @Schema(description = "Customer's phone number", example = "+254712345678")
    private String phoneNumber;

    @Schema(description = "Customer's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Date and time when the customer registered", example = "2023-01-15T10:30:00")
    private LocalDateTime registrationDate;

    @Schema(description = "Date and time of customer's last login", example = "2023-03-20T14:45:00")
    private LocalDateTime lastLogin;

    @Schema(description = "Profile image URL or data URL", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String profileImage;
}