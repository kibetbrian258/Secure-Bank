package com.application.secureBank.Services;

import com.application.secureBank.DTOs.CustomerRegistrationResponse;
import com.application.secureBank.DTOs.RegistrationRequest;
import com.application.secureBank.Repositories.CustomerRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

@Service
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountManagementService accountManagementService; // Changed from AccountService

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           AccountManagementService accountManagementService) { // Changed constructor parameter
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountManagementService = accountManagementService;
    }

    @Override
    public UserDetails loadUserByUsername(String customerId) throws UsernameNotFoundException {
        Customer customer = findByCustomerId(customerId);
        return new User(customer.getCustomerId(), customer.getPin(), new ArrayList<>());
    }

    public Customer findByCustomerId(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + customerId));
    }

    public void updateLastLogin(String customerId) {
        Customer customer = findByCustomerId(customerId);
        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Transactional
    public CustomerRegistrationResponse registerCustomer(RegistrationRequest request) {
        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Generate a 4-digit PIN
        String pin = generateRandomPin();

        // Generate customer ID with RD prefix and 4 digits
        String customerId = generateCustomerId();

        // Create customer entity
        Customer customer = Customer.builder()
                .customerId(customerId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .pin(passwordEncoder.encode(pin))
                .registrationDate(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // Create an account for the customer using the AccountManagementService
        Account account = accountManagementService.createAccount(savedCustomer);

        // Return registration response with plain text PIN for first-time login
        // and the generated account number
        return CustomerRegistrationResponse.builder()
                .customerId(customerId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .pin(pin)
                .accountNumber(account.getAccountNumber())
                .build();
    }

    /**
     * Generates a customer ID with format "RD" followed by 4 random digits
     */
    private String generateCustomerId() {
        Random random = new Random();
        String customerId;

        do {
            // Generate a number between 1000-9999 to ensure exactly 4 digits
            int randomNumber = 1000 + random.nextInt(9000);
            customerId = "RD" + randomNumber;
        } while (customerRepository.existsByCustomerId(customerId));

        return customerId;
    }

    /**
     * Generates a random 4-digit PIN
     */
    private String generateRandomPin() {
        Random random = new Random();
        // Generate a number between 1000-9999 to ensure exactly 4 digits
        int pin = 1000 + random.nextInt(9000);
        return String.valueOf(pin);
    }
}