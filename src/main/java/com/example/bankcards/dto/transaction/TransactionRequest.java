package com.example.bankcards.dto.transaction;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull(message = "The source card id is required")
    private Long sourceCardId;

    @NotNull(message = "The target card id is required")
    private Long targetCardId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer and 2 fraction digits")
    private BigDecimal amount;

}
