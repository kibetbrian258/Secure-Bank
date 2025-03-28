package com.application.secureBank.Services;

import com.application.secureBank.DTOs.AccountResponse;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    public AccountService(
            AccountRepository accountRepository,
            CustomerService customerService
    ) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }

    // Get all accounts for a customer
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        Customer customer = customerService.findByCustomerId(customerId);
        List<Account> accounts = accountRepository.findByCustomer(customer);

        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    // Get details of a specific account - FIXED to use direct repository lookup
    public AccountResponse getAccountDetails(String customerId, String accountNumber) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Direct repository lookup instead of filtering customer.getAccounts()
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();

        // Security check: verify account belongs to customer
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Account does not belong to this customer");
        }

        return mapToAccountResponse(account);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomer().getCustomerId())
                .balance(account.getBalance())
                .status(account.getStatus())
                .interestRate(account.getInterestRate())
                .branchName(account.getBranchName())
                .branchCode(account.getBranchCode())
                .onlineBanking(account.isOnlineBanking())
                .mobileBanking(account.isMobileBanking())
                .monthlyFee(account.getMonthlyFee())
                .minimumBalance(account.getMinimumBalance())
                .withdrawalLimit(account.getWithdrawalLimit())
                .transferLimit(account.getTransferLimit())
                .build();
    }

/**
 * Creates a new account for a customer with the specified account type
 * @param customerId The customer ID
 * @param accountType The type of account to create (savings or checking)
 * @return The account response with details of the created account
 */

public AccountResponse createAccountForCustomer(String customerId, String accountType) {
    Customer customer = customerService.findByCustomerId(customerId);

    // Set account parameters based on account type
    BigDecimal interestRate = BigDecimal.ZERO;
    BigDecimal monthlyFee = BigDecimal.ZERO;
    BigDecimal minimumBalance = BigDecimal.ZERO;

    if ("savings".equals(accountType)) {
        // Savings account parameters
        interestRate = new BigDecimal("2.5");
        minimumBalance = new BigDecimal("200.00");
    } else if ("checking".equals(accountType)) {
        // Checking account parameters
        monthlyFee = new BigDecimal("5.00");
    } else {
        throw new IllegalArgumentException("Invalid account type: " + accountType);
    }

    // Create the account
    Account account = Account.builder()
            .accountNumber(generateUniqueAccountNumber())
            .customer(customer)
            .balance(BigDecimal.ZERO)
            .status("Active")
            .interestRate(interestRate)
            .branchName("Main Branch")
            .branchCode("BR001")
            .onlineBanking(true)
            .mobileBanking(true)
            .monthlyFee(monthlyFee)
            .minimumBalance(minimumBalance)
            .withdrawalLimit(new BigDecimal("10000.00"))
            .transferLimit(new BigDecimal("10000.00"))
            .build();

    Account savedAccount = accountRepository.save(account);

    // Map to response DTO
    return mapToAccountResponse(savedAccount);
}

    /**
     * Generates a unique account number
     */
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;

        do {
            // Generate an 11-digit account number
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 11; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }

}