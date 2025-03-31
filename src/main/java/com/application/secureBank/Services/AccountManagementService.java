package com.application.secureBank.Services;

import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This service handles account creation functionality that was previously
 * causing circular dependencies between CustomerService and AccountService.
 */
@Service
public class AccountManagementService {

    private final AccountRepository accountRepository;

    public AccountManagementService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new account for a customer with a randomly generated account number
     */
    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public Account createAccount(Customer customer) {
        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .balance(new BigDecimal("0.00"))  // Initial balance
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

        return accountRepository.save(account);
    }

    /**
     * Generates a unique account number using ThreadLocalRandom for better performance
     * than standard Random in multi-threaded environments
     */
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