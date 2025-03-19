package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing account details")
public class AccountResponse {
    @Schema(description = "Unique identifier for the account", example = "1")
    private Long id;

    @Schema(description = "Account number", example = "47288276269")
    private String accountNumber;

    @Schema(description = "Customer ID who owns the account", example = "CU12783354")
    private String customerId;

    @Schema(description = "Current balance of the account", example = "5743.00")
    private BigDecimal balance;

    @Schema(description = "Status of the account (Active, Inactive, Frozen)", example = "Active")
    private String status;

    @Schema(description = "Interest rate for the account", example = "2.5")
    private BigDecimal interestRate;

    @Schema(description = "Name of the branch where the account was opened", example = "Main Branch")
    private String branchName;

    @Schema(description = "Code of the branch where the account was opened", example = "BR001")
    private String branchCode;

    @Schema(description = "Whether online banking is enabled for this account", example = "true")
    private boolean onlineBanking;

    @Schema(description = "Whether mobile banking is enabled for this account", example = "true")
    private boolean mobileBanking;

    @Schema(description = "Monthly fee for maintaining the account", example = "0.00")
    private BigDecimal monthlyFee;

    @Schema(description = "Minimum balance required for the account", example = "200.00")
    private BigDecimal minimumBalance;

    @Schema(description = "Maximum daily withdrawal limit", example = "10000.00")
    private BigDecimal withdrawalLimit;

    @Schema(description = "Maximum daily transfer limit", example = "10000.00")
    private BigDecimal transferLimit;
}