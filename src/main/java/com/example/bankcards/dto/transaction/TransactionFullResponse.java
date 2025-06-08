package com.example.bankcards.dto.transaction;

import com.example.bankcards.entity.transaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFullResponse {
    private Long id;
    private String sourceCardNumber;
    private String targetCardNumber;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
