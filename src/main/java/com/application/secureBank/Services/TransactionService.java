package com.application.secureBank.Services;

import com.application.secureBank.DTOs.DepositRequest;
import com.application.secureBank.DTOs.TransactionResponse;
import com.application.secureBank.DTOs.TransferRequest;
import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.Repositories.TransactionRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import com.application.secureBank.models.Transaction;
import org.springframework.stereotype.Service;
import com.application.secureBank.DTOs.WithdrawRequest;
import com.application.secureBank.DTOs.TransactionSearchRequest;
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

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CustomerService customerService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }
/*
    Deposit money to an account
 */
    public TransactionResponse deposit(String customerId, DepositRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        Optional<Account> accountOpt = customer.getAccounts().stream()
                .filter(acc -> acc.getAccountNumber().equals(request.getAccountNumber()))
                .findFirst();

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account Not Found");
        }

        Account account = accountOpt.get();

        //Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        //update account balance
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        //create transaction record
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

        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .accountNumber(account.getAccountNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .transactionDateTime(transaction.getTransactionDateTime())
                .status(transaction.getStatus())
                .build();
    }
/*
    Withdraw money from account
 */
    @Transactional
    public TransactionResponse withdraw(String customerId, WithdrawRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        Optional<Account> accountOpt = customer.getAccounts().stream()
                .filter(acc -> acc.getAccountNumber().equals(request.getAccountNumber()))
                .findFirst();

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();

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

        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .accountNumber(account.getAccountNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .transactionDateTime(transaction.getTransactionDateTime())
                .status(transaction.getStatus())
                .build();
    }

/*
Transfer money between accounts
*/

    @Transactional
    public TransactionResponse transfer(String customerId, TransferRequest request) {
        Customer customer = customerService.findByCustomerId(customerId);

        // Find source account
        Optional<Account> sourceAccountOpt = customer.getAccounts().stream()
                .filter(acc -> acc.getAccountNumber().equals(request.getSourceAccountNumber()))
                .findFirst();

        if (sourceAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Source account not found");
        }

        Account sourceAccount = sourceAccountOpt.get();

        // Find destination account
        List<Account> destinationAccounts = accountRepository.findByAccountNumber(
                request.getDestinationAccountNumber());

        if (destinationAccounts.isEmpty()) {
            throw new IllegalArgumentException("Destination account not found");
        }

        Account destinationAccount = destinationAccounts.get(0);

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
        accountRepository.save(sourceAccount);

        // Update destination account balance
        BigDecimal newDestBalance = destinationAccount.getBalance().add(request.getAmount());
        destinationAccount.setBalance(newDestBalance);
        accountRepository.save(destinationAccount);

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

        transactionRepository.save(sourceTransaction);

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

        transactionRepository.save(destTransaction);

        return TransactionResponse.builder()
                .transactionId(sourceTransaction.getTransactionId())
                .accountNumber(sourceAccount.getAccountNumber())
                .type(sourceTransaction.getType())
                .amount(sourceTransaction.getAmount())
                .balanceAfterTransaction(sourceTransaction.getBalanceAfterTransaction())
                .transactionDateTime(sourceTransaction.getTransactionDateTime())
                .status(sourceTransaction.getStatus())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .build();
    }

    /*
    Get the last 10 transactions
    */

    public List<TransactionResponse> getLastTenTransactions(String customerId, String accountNumber) {
        Customer customer = customerService.findByCustomerId(customerId);

        Optional<Account> accountOpt = customer.getAccounts().stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst();

        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();

        List<Transaction> transactions = transactionRepository
                .findTop10ByAccountOrderByTransactionDateTimeDesc(account.getId());

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /*
    search transaction by criteria
     */

    public List<TransactionResponse> searchTransactions(
            String customerId,
            TransactionSearchRequest searchRequest) {

        Customer customer = customerService.findByCustomerId(customerId);

        // Get all customer accounts
        List<Account> accounts = accountRepository.findByCustomer(customer);

        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No accounts found for customer");
        }

        // Build search criteria based on input
        List<Transaction> transactions = transactionRepository.findAll();

        // Filter by account if specified
        if (searchRequest.getAccountNumber() != null && !searchRequest.getAccountNumber().isEmpty()) {
            String accountNumber = searchRequest.getAccountNumber();
            transactions = transactions.stream()
                    .filter(t -> t.getAccount().getAccountNumber().equals(accountNumber))
                    .collect(Collectors.toList());
        } else {
            // Otherwise filter for any account owned by this customer
            List<Long> accountIds = accounts.stream()
                    .map(Account::getId)
                    .collect(Collectors.toList());

            transactions = transactions.stream()
                    .filter(t -> accountIds.contains(t.getAccount().getId()))
                    .collect(Collectors.toList());
        }

        // Filter by transaction type if specified
        if (searchRequest.getType() != null && !searchRequest.getType().isEmpty()) {
            String type = searchRequest.getType();
            transactions = transactions.stream()
                    .filter(t -> t.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        // Filter by date range if specified
        if (searchRequest.getStartDate() != null) {
            LocalDateTime startDate = searchRequest.getStartDate();
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionDateTime().isAfter(startDate) ||
                            t.getTransactionDateTime().isEqual(startDate))
                    .collect(Collectors.toList());
        }

        if (searchRequest.getEndDate() != null) {
            LocalDateTime endDate = searchRequest.getEndDate();
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionDateTime().isBefore(endDate) ||
                            t.getTransactionDateTime().isEqual(endDate))
                    .collect(Collectors.toList());
        }

        // Filter by amount range if specified
        if (searchRequest.getMinAmount() != null) {
            BigDecimal minAmount = searchRequest.getMinAmount();
            transactions = transactions.stream()
                    .filter(t -> t.getAmount().compareTo(minAmount) >= 0)
                    .collect(Collectors.toList());
        }

        if (searchRequest.getMaxAmount() != null) {
            BigDecimal maxAmount = searchRequest.getMaxAmount();
            transactions = transactions.stream()
                    .filter(t -> t.getAmount().compareTo(maxAmount) <= 0)
                    .collect(Collectors.toList());
        }

        // Map to DTOs and return
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
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
        return "TRANS" + String.format("%05d", new Random().nextInt(100000));
    }
}
