package com.application.secureBank.Services;

import com.application.secureBank.DTOs.CustomerRegistrationResponse;
import com.application.secureBank.DTOs.RegistrationRequest;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.Repositories.CustomerRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

@Service
public class CustomerService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Use constructor instead of @RequiredArgsConstructor
    public CustomerService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String customerId) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + customerId));

        return new User(
                customer.getCustomerId(),
                customer.getPin(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public CustomerRegistrationResponse registerCustomer(RegistrationRequest request) {
        // Generate unique customer id
        String customerId = generateCustomerId();
        while (customerRepository.existsByCustomerId(customerId)) {
            customerId = generateCustomerId();
        }

        // Generate pin
        String plainPin = generatePin();
        String encodedPin = passwordEncoder.encode(plainPin);

        Customer customer = Customer.builder()
                .customerId(customerId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .pin(encodedPin)  // Store ENCODED pin in database
                .registrationDate(LocalDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // Return DTO with plain pin for display purposes only
        return CustomerRegistrationResponse.builder()
                .customerId(savedCustomer.getCustomerId())
                .fullName(savedCustomer.getFullName())
                .email(savedCustomer.getEmail())
                .pin(plainPin)  // Return PLAIN pin in response
                .build();
    }

    public Customer findByCustomerId(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + customerId));
    }

    public boolean checkCredentials(String customerId, String pin) {
        Customer customer = findByCustomerId(customerId);
        return passwordEncoder.matches(pin, customer.getPin());
    }

    @Transactional
    public void updateLastLogin(String customerId) {
        Customer customer = findByCustomerId(customerId);
        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private String generateCustomerId() {
        return "RD" + String.format("%04d", new Random().nextInt(10000));
    }

    private String generatePin() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private String generateAccountNumber() {
        return String.format("%010d", new Random().nextInt(1000000000));
    }
}