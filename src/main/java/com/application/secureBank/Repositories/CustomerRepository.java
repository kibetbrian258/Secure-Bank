package com.application.secureBank.Repositories;

import com.application.secureBank.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCustomerId(String customerId);
}
