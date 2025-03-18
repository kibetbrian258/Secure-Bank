package com.application.secureBank.Controllers;

import com.application.secureBank.DTOs.*;
import com.application.secureBank.Services.CustomerService;
import com.application.secureBank.Services.JwtService;
import com.application.secureBank.models.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerService customerService;
    private final JwtService jwtService;

    @PostMapping("/register")
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
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginCustomer(
            @Valid
            @RequestBody
            LoginRequest loginRequest
    ) {
        JwtResponse jwtResponse = jwtService.authenticateCustomer(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}
