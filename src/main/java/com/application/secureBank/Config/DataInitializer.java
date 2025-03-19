package com.application.secureBank.Config;

import com.application.secureBank.Repositories.AccountRepository;
import com.application.secureBank.Repositories.CustomerRepository;
import com.application.secureBank.Repositories.TransactionRepository;
import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import com.application.secureBank.models.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    @Profile("!prod")
    public CommandLineRunner initData(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Generate a test customer
            if (customerRepository.count() == 0) {
                Customer customer = Customer.builder()
                        .customerId("CU12783354")
                        .fullName("John Doe")
                        .email("john.doe@example.com")
                        .pin(passwordEncoder.encode("1234"))
                        .address("123 Main St, City")
                        .phoneNumber("+1234567890")
                        .registrationDate(LocalDateTime.now().minusDays(30))
                        .lastLogin(LocalDateTime.now())
                        .build();

                customerRepository.save(customer);

                // Create an account for the customer
                Account account = Account.builder()
                        .accountNumber("47288276269")
                        .customer(customer)
                        .balance(new BigDecimal("5743.00"))
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

                accountRepository.save(account);

                // Create some sample transactions
                createSampleTransaction(
                        transactionRepository,
                        account,
                        "Deposit",
                        new BigDecimal("1000.00"),
                        new BigDecimal("10545.00"),
                        LocalDateTime.now().minusDays(7));

                createSampleTransaction(
                        transactionRepository,
                        account,
                        "Withdrawal",
                        new BigDecimal("1500.00"),
                        new BigDecimal("9489.00"),
                        LocalDateTime.now().minusDays(6));

                createSampleTransaction(
                        transactionRepository,
                        account,
                        "Deposit",
                        new BigDecimal("2500.00"),
                        new BigDecimal("11545.00"),
                        LocalDateTime.now().minusDays(2));

                // Create a second account for testing transfers
                Account secondAccount = Account.builder()
                        .accountNumber("47288276270")
                        .customer(customer)
                        .balance(new BigDecimal("2000.00"))
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

                accountRepository.save(secondAccount);

                // Create a transfer transaction
                createSampleTransferTransaction(
                        transactionRepository,
                        account,
                        secondAccount,
                        new BigDecimal("500.00"),
                        new BigDecimal("11045.00"),
                        new BigDecimal("2500.00"),
                        LocalDateTime.now().minusDays(1));

                System.out.println("Initialized sample data successfully");
            }
        };
    }

    /**
     * Creates a sample transaction with a generated transaction ID
     */
    private void createSampleTransaction(
            TransactionRepository transactionRepository,
            Account account,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            LocalDateTime dateTime) {

        String transactionId = generateUniqueTransactionId();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfter)
                .transactionDateTime(dateTime)
                .status("Completed")
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Creates a sample transaction with destination account
     */
    private void createSampleTransaction(
            TransactionRepository transactionRepository,
            Account account,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            LocalDateTime dateTime,
            String destinationAccountNumber) {

        String transactionId = generateUniqueTransactionId();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfter)
                .transactionDateTime(dateTime)
                .status("Completed")
                .destinationAccountNumber(destinationAccountNumber)
                .build();

        transactionRepository.save(transaction);
    }

    /*
     * Creates a pair of transfer transactions between two accounts
     */
    private void createSampleTransferTransaction(
            TransactionRepository transactionRepository,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            BigDecimal sourceBalanceAfter,
            BigDecimal destBalanceAfter,
            LocalDateTime dateTime) {

        // Create source account transfer record
        createSampleTransaction(
                transactionRepository,
                sourceAccount,
                "Transfer",
                amount,
                sourceBalanceAfter,
                dateTime,
                destinationAccount.getAccountNumber());

        // Create destination account transfer received record
        createSampleTransaction(
                transactionRepository,
                destinationAccount,
                "Transfer Received",
                amount,
                destBalanceAfter,
                dateTime);
    }

    /*
     * Generates a unique transaction ID
     */
    private String generateUniqueTransactionId() {
        return "TRANS" + String.format("%05d", new Random().nextInt(100000));
    }
}