package com.application.secureBank.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private String status = "Active";

    @Builder.Default
    private BigDecimal interestRate = new BigDecimal("2.5");

    @Builder.Default
    private String branchName = "Main Branch";

    @Builder.Default
    private String branchCode = "BR001";

    @Builder.Default
    private boolean onlineBanking = true;

    @Builder.Default
    private boolean mobileBanking = true;

    @Builder.Default
    private BigDecimal monthlyFee = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal minimumBalance = new BigDecimal("200.00");

    @Builder.Default
    private BigDecimal withdrawalLimit = new BigDecimal("10000.00");

    @Builder.Default
    private BigDecimal transferLimit = new BigDecimal("10000.00");

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private Set<Transaction> transactions = new HashSet<>();
}
