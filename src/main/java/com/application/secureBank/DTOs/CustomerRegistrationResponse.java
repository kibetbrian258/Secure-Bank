package com.application.secureBank.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationResponse {
    private String customerId;
    private String fullName;
    private String email;
    private String pin;
    private String accountNumber;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
}