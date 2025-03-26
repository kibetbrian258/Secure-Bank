package com.application.secureBank.DTOs;

import com.application.secureBank.validators.ValidEastAfricanPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Request for customer registration")
public class RegistrationRequest {
    @Schema(description = "Full name of the customer", example = "John Doe", required = true)
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Schema(description = "Email address of the customer", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Date of birth of the customer", example = "1990-01-15", required = true)
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Schema(description = "Phone number in East African international format", example = "+254712345678", required = true)
    @NotBlank(message = "Phone number is required")
    @ValidEastAfricanPhone
    private String phoneNumber;

    @Schema(description = "Customer's address", example = "123 Main St, City, State, ZIP", required = true)
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters")
    private String address;
}