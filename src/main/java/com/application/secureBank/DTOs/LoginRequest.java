package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for customer authentication")
public class LoginRequest {
    @Schema(description = "Customer ID for authentication", example = "CU12783354", required = true)
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @Schema(description = "PIN for authentication", example = "1234", required = true)
    @NotBlank(message = "PIN is required")
    private String pin;
}