package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private String ownerName;
    private String expirationDate;
    private BigDecimal balance;
    private CardStatus status;
}
