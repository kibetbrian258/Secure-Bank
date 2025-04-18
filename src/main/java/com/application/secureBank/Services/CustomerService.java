package com.application.secureBank.Services;

import com.application.secureBank.DTOs.CustomerProfileResponse;
import com.application.secureBank.DTOs.CustomerRegistrationResponse;
import com.application.secureBank.DTOs.RegistrationRequest;
import com.application.secureBank.DTOs.UpdateProfileRequest;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.Repositories.CustomerRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PhoneNumberService phoneNumberService;
    private final AccountRepository accountRepository;
    private final ProfileImageService profileImageService;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           AccountManagementService accountManagementService,
                           EmailService emailService,
                           PhoneNumberService phoneNumberService,
                           AccountRepository accountRepository,
                           ProfileImageService profileImageService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.phoneNumberService = phoneNumberService;
        this.accountRepository = accountRepository;
        this.profileImageService = profileImageService;
    }

    @Override
    @Cacheable(value = "userDetails", key = "#customerId")
    public UserDetails loadUserByUsername(String customerId) throws UsernameNotFoundException {
        customerId = customerId != null ? customerId.trim() : customerId;
        Customer customer = findByCustomerId(customerId);
        return new User(customer.getCustomerId(), customer.getPin(), new ArrayList<>());
    }

    @Cacheable(value = "customers", key = "#customerId")
    public Customer findByCustomerId(String customerId) {
        String trimmedCustomerId = customerId != null ? customerId.trim() : customerId;

        return customerRepository.findByCustomerId(trimmedCustomerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + trimmedCustomerId));
    }

    @Transactional
    @CacheEvict(value = {"customers", "userDetails"}, key = "#customerId")
    public void updateLastLogin(String customerId) {
        // Using direct database update instead of fetch-modify-save for better performance
        customerRepository.updateLastLogin(customerId, LocalDateTime.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomerRegistrationResponse registerCustomer(RegistrationRequest request) {
        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        try {
            // Generate a 4-digit PIN
            String pin = generateRandomPin();

            // Generate customer ID with RD prefix and 4 digits
            String customerId = generateCustomerId();

            // Format the phone number
            String formattedPhoneNumber = phoneNumberService.formatKenyanPhoneNumber(request.getPhoneNumber());

            // Create customer entity
            Customer customer = Customer.builder()
                    .customerId(customerId)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .pin(passwordEncoder.encode(pin))
                    .registrationDate(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .dateOfBirth(request.getDateOfBirth() != null
                            ? request.getDateOfBirth().atStartOfDay()
                            : null)
                    .phoneNumber(formattedPhoneNumber)
                    .address(request.getAddress())
                    .build();

            // Save customer
            Customer savedCustomer = customerRepository.save(customer);

            // IMPORTANT: Flush to ensure customer is written to database
            customerRepository.flush();

            // Create an account manually (not in a new transaction)
            Account account = Account.builder()
                    .accountNumber(generateUniqueAccountNumber())
                    .customer(savedCustomer)
                    .balance(new BigDecimal("0.00"))
                    .status("Active")
                    .interestRate(new BigDecimal("2.5"))
                    .branchName("Main Branch")
                    .branchCode("BR001")
                    .onlineBanking(true)
                    .mobileBanking(true)
                    .monthlyFee(BigDecimal.ZERO)
                    .minimumBalance(new BigDecimal("200.00"))
                    .withdrawalLimit(new BigDecimal("10000.00"))
                    .transferLimit(new BigDecimal("10000.00"))
                    .build();

            Account savedAccount = accountRepository.save(account);

            // Prepare response
            CustomerRegistrationResponse response = CustomerRegistrationResponse.builder()
                    .customerId(customerId)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .pin(pin)
                    .accountNumber(savedAccount.getAccountNumber())
                    .dateOfBirth(request.getDateOfBirth())
                    .phoneNumber(phoneNumberService.extractSignificantDigits(formattedPhoneNumber))
                    .address(request.getAddress())
                    .build();

            // Send email AFTER transaction completion (this remains unchanged)
            CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendRegistrationEmail(
                            request.getEmail(),
                            request.getFullName(),
                            customerId,
                            pin,
                            savedAccount.getAccountNumber()
                    );
                } catch (Exception ex) {
                    log.error("Failed to send registration email: {}", ex.getMessage());
                }
            });

            return response;
        } catch (Exception e) {
            // Log the specific error for debugging
            log.error("Error during customer registration: {}", e.getMessage(), e);
            throw e;
        }
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

    /**
     * Updates a customer's profile information
     * @param customerId The customer ID
     * @param request The update profile request containing fields to update
     * @return The updated customer profile response
     */
    @Transactional
    @CacheEvict(value = {"customers", "userDetails"}, key = "#customerId")
    public CustomerProfileResponse updateCustomerProfile(String customerId, UpdateProfileRequest request) {
        Customer customer = findByCustomerId(customerId);

        // Only update fields that are provided in the request (not null)
        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }

        if (request.getEmail() != null) {

            if (!request.getEmail().equals(customer.getEmail()) &&
                    customerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            customer.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            String formattedPhoneNumber = phoneNumberService.formatKenyanPhoneNumber(request.getPhoneNumber());
            customer.setPhoneNumber(formattedPhoneNumber);
        }

        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }

        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth().atStartOfDay());
        }

        // Save the updated customer
        Customer savedCustomer = customerRepository.save(customer);

        // Map to profile response
        return CustomerProfileResponse.builder()
                .customerId(savedCustomer.getCustomerId())
                .fullName(savedCustomer.getFullName())
                .email(savedCustomer.getEmail())
                .address(savedCustomer.getAddress())
                .phoneNumber(savedCustomer.getPhoneNumber()) // Full formatted phone with prefix
                .dateOfBirth(savedCustomer.getDateOfBirth() != null
                        ? savedCustomer.getDateOfBirth().toLocalDate()
                        : null)
                .registrationDate(savedCustomer.getRegistrationDate())
                .lastLogin(savedCustomer.getLastLogin())
                .build();
    }

    //  generate unique account numbers
    private String generateUniqueAccountNumber() {
        String accountNumber;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        do {
            // Generate an 11-digit account number
            StringBuilder sb = new StringBuilder(11);
            for (int i = 0; i < 11; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }
}