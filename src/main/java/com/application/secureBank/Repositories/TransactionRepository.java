package com.application.secureBank.Repositories;

import com.application.secureBank.models.Account;
import com.application.secureBank.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    Optional<Transaction> findByTransactionId(String transactionId);

    @Query(value = "SELECT * FROM transactions WHERE account_id = ?1 ORDER BY transaction_date_time DESC LIMIT 10", nativeQuery = true)
    List<Transaction> findTop10ByAccountOrderByTransactionDateTimeDesc(Long accountId);
}
