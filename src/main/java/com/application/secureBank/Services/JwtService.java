package com.application.secureBank.Services;

import com.application.secureBank.Config.JwtTokenProvider;
import com.application.secureBank.DTOs.JwtResponse;
import com.application.secureBank.DTOs.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerService customerService;

    public JwtResponse authenticateCustomer(LoginRequest loginRequest) {
        log.info("Authenticating customer: {}", loginRequest.getCustomerId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCustomerId(),
                        loginRequest.getPin()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(loginRequest.getCustomerId());

        customerService.updateLastLogin(loginRequest.getCustomerId());

        return JwtResponse.builder()
                .token(jwt)
                .customerId(loginRequest.getCustomerId())
                .build();
    }
}