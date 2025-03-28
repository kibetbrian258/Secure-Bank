package com.application.secureBank.DTOs;

import com.application.secureBank.validators.ValidEastAfricanPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update customer profile")
public class UpdateProfileRequest {

    @Schema(description = "Customer's full name", example = "John Doe")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Customer's phone number in East African format", example = "+254712345678")
    @ValidEastAfricanPhone
    private String phoneNumber;

    @Schema(description = "Customer's address", example = "123 Main St, City")
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Schema(description = "Customer's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;
}