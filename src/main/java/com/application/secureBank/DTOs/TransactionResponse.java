package com.application.secureBank.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing transaction details")
public class TransactionResponse {
    @Schema(description = "Unique transaction identifier", example = "TRANS12345")
    private String transactionId;

    @Schema(description = "Account number involved in the transaction", example = "47288276269")
    private String accountNumber;

    @Schema(description = "Type of transaction (Deposit, Withdrawal, Transfer, Transfer Received)", example = "Deposit")
    private String type;

    @Schema(description = "Transaction amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Account balance after transaction", example = "6743.00")
    private BigDecimal balanceAfterTransaction;

    @Schema(description = "Date and time when the transaction occurred", example = "2025-03-19T14:30:00")
    private LocalDateTime transactionDateTime;

    @Schema(description = "Status of the transaction (Completed, Pending, Failed)", example = "Completed")
    private String status;

    @Schema(description = "For transfer transactions, the destination account number", example = "47288276270")
    private String destinationAccountNumber;
}