package com.application.secureBank.Repositories;

import com.application.secureBank.models.Account;
import com.application.secureBank.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    Optional<Transaction> findByTransactionId(String transactionId);

    // Find last 10 transactions, more efficient using pagination
    @Query(value = "SELECT * FROM transactions WHERE account_id = :accountId ORDER BY transaction_date_time DESC LIMIT 10",
            nativeQuery = true)
    List<Transaction> findTop10ByAccountOrderByTransactionDateTimeDesc(@Param("accountId") Long accountId);

    // Paginated version for more flexibility
    Page<Transaction> findByAccountOrderByTransactionDateTimeDesc(Account account, Pageable pageable);

    // Search methods that leverage database filtering instead of in-memory filtering

    // Find transactions for an account number
    List<Transaction> findByAccount_AccountNumber(String accountNumber);

    // Find transactions by type
    List<Transaction> findByAccount_AccountNumberAndType(String accountNumber, String type);

    // Find transactions by date range
    List<Transaction> findByAccount_AccountNumberAndTransactionDateTimeBetween(
            String accountNumber, LocalDateTime startDate, LocalDateTime endDate);

    // Find transactions by amount range
    List<Transaction> findByAccount_AccountNumberAndAmountBetween(
            String accountNumber, BigDecimal minAmount, BigDecimal maxAmount);

    // Complex parameterized query to handle multiple optional search criteria
    @Query("SELECT t FROM Transaction t WHERE " +
            "t.account.customer.id = :customerId " +
            "AND (:accountNumber IS NULL OR t.account.accountNumber = :accountNumber) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (COALESCE(:startDate, NULL) IS NULL OR t.transactionDateTime >= :startDate) " +
            "AND (COALESCE(:endDate, NULL) IS NULL OR t.transactionDateTime <= :endDate) " +
            "AND (COALESCE(:minAmount, NULL) IS NULL OR t.amount >= :minAmount) " +
            "AND (COALESCE(:maxAmount, NULL) IS NULL OR t.amount <= :maxAmount) " +
            "ORDER BY t.transactionDateTime DESC")
    List<Transaction> searchTransactions(
            @Param("customerId") Long customerId,
            @Param("accountNumber") String accountNumber,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    // Paginated version of the search query
    @Query("SELECT t FROM Transaction t WHERE " +
            "t.account.customer.id = :customerId " +
            "AND (:accountNumber IS NULL OR t.account.accountNumber = :accountNumber) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (COALESCE(:startDate, NULL) IS NULL OR t.transactionDateTime >= :startDate) " +
            "AND (COALESCE(:endDate, NULL) IS NULL OR t.transactionDateTime <= :endDate) " +
            "AND (COALESCE(:minAmount, NULL) IS NULL OR t.amount >= :minAmount) " +
            "AND (COALESCE(:maxAmount, NULL) IS NULL OR t.amount <= :maxAmount) " +
            "ORDER BY t.transactionDateTime DESC")
    Page<Transaction> searchTransactionsPaginated(
            @Param("customerId") Long customerId,
            @Param("accountNumber") String accountNumber,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);
}