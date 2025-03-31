package com.application.secureBank.Services;

import com.application.secureBank.DTOs.DepositRequest;
import com.application.secureBank.DTOs.TransactionResponse;
import com.application.secureBank.DTOs.TransferRequest;
import com.application.secureBank.DTOs.WithdrawRequest;
import com.application.secureBank.DTOs.TransactionSearchRequest;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.Repositories.TransactionRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import com.application.secureBank.models.Transaction;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerService customerService;
    private final Random random = new Random();

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CustomerService customerService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }

    /**
     * Deposit money to an account
     */
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, allEntries = true)
    public TransactionResponse deposit(String customerId, DepositRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Direct repository lookup instead of filtering customer accounts
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account Not Found");
        }

        Account account = accountOpt.get();

        // Security check: verify the account belongs to the customer
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Account does not belong to this customer");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        // Update account balance
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .account(account)
                .type("Deposit")
                .amount(request.getAmount())
                .balanceAfterTransaction(newBalance)
                .transactionDateTime(LocalDateTime.now())
                .status("Completed")
                .build();

        transactionRepository.save(transaction);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, allEntries = true)
    public TransactionResponse withdraw(String customerId, WithdrawRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Direct repository lookup
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();

        // Security check: verify account belongs to customer
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Account does not belong to this customer");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        // Check if account has sufficient balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Check if withdrawal is within the limit
        if (request.getAmount().compareTo(account.getWithdrawalLimit()) > 0) {
            throw new IllegalArgumentException("Amount exceeds withdrawal limit of " +
                    account.getWithdrawalLimit());
        }

        // Update account balance
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .account(account)
                .type("Withdrawal")
                .amount(request.getAmount())
                .balanceAfterTransaction(newBalance)
                .transactionDateTime(LocalDateTime.now())
                .status("Completed")
                .build();

        transactionRepository.save(transaction);

        return mapToTransactionResponse(transaction);
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, allEntries = true)
    public TransactionResponse transfer(String customerId, TransferRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Find source account using direct repository query
        Optional<Account> sourceAccountOpt = accountRepository.findByAccountNumber(
                request.getSourceAccountNumber());

        if (sourceAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Source account not found");
        }

        Account sourceAccount = sourceAccountOpt.get();

        // Security check: verify source account belongs to customer
        if (!sourceAccount.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Source account does not belong to this customer");
        }

        // Find destination account using direct repository query
        Optional<Account> destinationAccountOpt = accountRepository.findByAccountNumber(
                request.getDestinationAccountNumber());

        if (destinationAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Destination account not found");
        }

        Account destinationAccount = destinationAccountOpt.get();

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // Check if source account has sufficient balance
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account");
        }

        // Check if transfer is within the limit
        if (request.getAmount().compareTo(sourceAccount.getTransferLimit()) > 0) {
            throw new IllegalArgumentException("Amount exceeds transfer limit of " +
                    sourceAccount.getTransferLimit());
        }

        // Update source account balance
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(newSourceBalance);

        // Update destination account balance
        BigDecimal newDestBalance = destinationAccount.getBalance().add(request.getAmount());
        destinationAccount.setBalance(newDestBalance);

        // Save both accounts in one batch
        accountRepository.saveAll(List.of(sourceAccount, destinationAccount));

        // Create transaction record for source account
        Transaction sourceTransaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .account(sourceAccount)
                .type("Transfer")
                .amount(request.getAmount())
                .balanceAfterTransaction(newSourceBalance)
                .transactionDateTime(LocalDateTime.now())
                .status("Completed")
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .build();

        // Create transaction record for destination account
        Transaction destTransaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .account(destinationAccount)
                .type("Transfer Received")
                .amount(request.getAmount())
                .balanceAfterTransaction(newDestBalance)
                .transactionDateTime(LocalDateTime.now())
                .status("Completed")
                .build();

        // Save both transactions
        transactionRepository.saveAll(List.of(sourceTransaction, destTransaction));

        return mapToTransactionResponse(sourceTransaction);
    }

    /**
     * Get the last 10 transactions
     */
    @Cacheable(value = "transactions", key = "'recent_' + #accountNumber")
    public List<TransactionResponse> getLastTenTransactions(String customerId, String accountNumber) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Direct repository lookup
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();

        // Security check: verify account belongs to customer
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Account does not belong to this customer");
        }

        List<Transaction> transactions = transactionRepository
                .findTop10ByAccountOrderByTransactionDateTimeDesc(account.getId());

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search transactions by criteria
     */
    @Cacheable(value = "transactions", key = "'search_' + #customerId")
    public List<TransactionResponse> searchTransactions(
            String customerId,
            TransactionSearchRequest searchRequest) {

        Customer customer = customerService.findByCustomerId(customerId);

        // Prepare parameters to avoid NULL issues with COALESCE
        String accountNumber = searchRequest.getAccountNumber();
        String type = searchRequest.getType();
        LocalDateTime startDate = searchRequest.getStartDate();
        LocalDateTime endDate = searchRequest.getEndDate();
        BigDecimal minAmount = searchRequest.getMinAmount();
        BigDecimal maxAmount = searchRequest.getMaxAmount();

        // Use the repository method that does all filtering in the database
        List<Transaction> transactions = transactionRepository.searchTransactions(
                customer.getId(),
                accountNumber,
                type,
                startDate,
                endDate,
                minAmount,
                maxAmount);

        // Map the results to DTOs
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Paginated version of the transaction search
     */
    public Page<TransactionResponse> searchTransactionsPaginated(
            String customerId,
            TransactionSearchRequest searchRequest,
            int page,
            int size) {

        Customer customer = customerService.findByCustomerId(customerId);

        // Create a pageable request WITHOUT sorting - we'll let the query handle the sorting
        Pageable pageable = PageRequest.of(page, size);

        // Handle null parameters explicitly
        String accountNumber = null;
        if (searchRequest.getAccountNumber() != null && !searchRequest.getAccountNumber().trim().isEmpty()) {
            accountNumber = searchRequest.getAccountNumber();
        }

        String type = null;
        if (searchRequest.getType() != null && !searchRequest.getType().trim().isEmpty()) {
            type = searchRequest.getType();
        }

        LocalDateTime startDate = searchRequest.getStartDate();
        LocalDateTime endDate = searchRequest.getEndDate();

        // Set BigDecimal parameters to null if not provided
        BigDecimal minAmount = searchRequest.getMinAmount();
        BigDecimal maxAmount = searchRequest.getMaxAmount();

        try {
            // Use the paginated repository method
            Page<Transaction> transactionsPage = transactionRepository.searchTransactionsPaginated(
                    customer.getId(),
                    accountNumber,
                    type,
                    startDate,
                    endDate,
                    minAmount,
                    maxAmount,
                    pageable);

            // Map the results to DTOs
            return transactionsPage.map(this::mapToTransactionResponse);
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in searchTransactionsPaginated: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Helper method to map Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .transactionDateTime(transaction.getTransactionDateTime())
                .status(transaction.getStatus())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .build();
    }

    /**
     * Generate a unique transaction ID
     */
    private String generateTransactionId() {
        return "TRANS" + String.format("%05d", random.nextInt(100000));
    }
}