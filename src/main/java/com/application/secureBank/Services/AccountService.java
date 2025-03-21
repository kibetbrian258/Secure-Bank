package com.application.secureBank.Services;

import com.application.secureBank.DTOs.AccountResponse;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
}