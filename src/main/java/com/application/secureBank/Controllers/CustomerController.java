package com.application.secureBank.Controllers;

import com.application.secureBank.DTOs.CustomerProfileResponse;
import com.application.secureBank.DTOs.UpdateProfileRequest;
import com.application.secureBank.Services.CustomerService;
import com.application.secureBank.models.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "API endpoints for customer profile management")
@SecurityRequirement(name = "BearerAuth")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/profile")
    @Operation(
            summary = "Get customer profile",
            description = "Retrieves the authenticated customer's profile information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        Customer customer = customerService.findByCustomerId(customerId);

        // Map to DTO to avoid exposing sensitive fields, now including new fields
        CustomerProfileResponse profileResponse = CustomerProfileResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth() != null
                        ? customer.getDateOfBirth().toLocalDate()
                        : null)
                .registrationDate(customer.getRegistrationDate())
                .lastLogin(customer.getLastLogin())
                .build();

        return ResponseEntity.ok(profileResponse);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update customer profile",
            description = "Updates the authenticated customer's profile information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid profile data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<CustomerProfileResponse> updateCustomerProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        CustomerProfileResponse updatedProfile = customerService.updateCustomerProfile(customerId, request);
        return ResponseEntity.ok(updatedProfile);
    }
}