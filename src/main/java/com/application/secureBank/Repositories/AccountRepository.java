package com.application.secureBank.Repositories;

import com.application.secureBank.models.Account;
import com.application.secureBank.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Find all accounts belonging to a customer
     * @param customer The customer entity
     * @return List of accounts
     */
    List<Account> findByCustomer(Customer customer);

    /**
     * Find an account by its account number
     * @param accountNumber The account number
     * @return Optional containing the account if found, empty otherwise
     */
    Optional<Account> findByAccountNumber(String accountNumber);
}