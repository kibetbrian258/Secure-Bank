package com.application.secureBank.Repositories;

import com.application.secureBank.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCustomerId(String customerId);

    /**
     * Updates the last login time for a customer directly in the database
     * This avoids the need to fetch the entity, update it, and save it back
     *
     * @param customerId The customer ID
     * @param lastLogin The last login timestamp
     */
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.lastLogin = :lastLogin WHERE c.customerId = :customerId")
    void updateLastLogin(@Param("customerId") String customerId, @Param("lastLogin") LocalDateTime lastLogin);
}