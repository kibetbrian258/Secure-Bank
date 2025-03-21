package com.application.secureBank.Controllers;

import com.application.secureBank.DTOs.*;
import com.application.secureBank.Services.CustomerService;
import com.application.secureBank.Services.JwtService;
import com.application.secureBank.models.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API endpoints for customer registration and authentication")
public class AuthController {
    private final CustomerService customerService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new customer",
            description = "Creates a new customer account with the provided information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid
            @RequestBody
            RegistrationRequest registrationRequest
    ) {
        CustomerRegistrationResponse registrationResponse = customerService.registerCustomer(registrationRequest);

        CustomerResponse response = CustomerResponse.builder()
                .customerId(registrationResponse.getCustomerId())
                .pin(registrationResponse.getPin())
                .fullName(registrationResponse.getFullName())
                .email(registrationResponse.getEmail())
                .accountNumber(registrationResponse.getAccountNumber())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a customer",
            description = "Authenticates a customer using their customer ID and PIN, returning a JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<JwtResponse> loginCustomer(
            @Valid
            @RequestBody
            LoginRequest loginRequest
    ) {
        JwtResponse jwtResponse = jwtService.authenticateCustomer(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}