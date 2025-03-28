package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new account")
public class CreateAccountRequest {

    @Schema(description = "Type of account to create (savings or checking)", example = "savings", required = true)
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(savings|checking)$", message = "Account type must be 'savings' or 'checking'")
    private String accountType;
}