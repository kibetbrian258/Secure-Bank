package com.application.secureBank.Controllers;

import com.application.secureBank.DTOs.*;
import com.application.secureBank.Services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "API endpoints for account transactions including deposits, withdrawals, transfers, and transaction history")
@SecurityRequirement(name = "BearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(
            summary = "Deposit funds",
            description = "Deposit money into a customer account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid deposit request",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found or doesn't belong to the customer",
                    content = @Content
            )
    })
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        TransactionResponse response = transactionService.deposit(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(
            summary = "Withdraw funds",
            description = "Withdraw money from a customer account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid withdrawal request or insufficient funds",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found or doesn't belong to the customer",
                    content = @Content
            )
    })
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        TransactionResponse response = transactionService.withdraw(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(
            summary = "Transfer funds",
            description = "Transfer money between accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer request, insufficient funds, or same account transfer",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found or source account doesn't belong to the customer",
                    content = @Content
            )
    })
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        TransactionResponse response = transactionService.transfer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent/{accountNumber}")
    @Operation(
            summary = "Get recent transactions",
            description = "Get the last 10 transactions for an account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recent transactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found or doesn't belong to the customer",
                    content = @Content
            )
    })
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @Parameter(description = "Account number to retrieve transactions for", required = true)
            @PathVariable String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        List<TransactionResponse> transactions = transactionService.getLastTenTransactions(customerId, accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/search")
    @Operation(
            summary = "Search transactions",
            description = "Search transactions by various criteria including date range, amount range, and transaction type"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found or doesn't belong to the customer",
                    content = @Content
            )
    })
    public ResponseEntity<List<TransactionResponse>> searchTransactions(
            @Valid @RequestBody TransactionSearchRequest searchRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();

        List<TransactionResponse> transactions = transactionService.searchTransactions(customerId, searchRequest);
        return ResponseEntity.ok(transactions);
    }
}