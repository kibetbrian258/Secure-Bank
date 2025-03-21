package com.application.secureBank.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}