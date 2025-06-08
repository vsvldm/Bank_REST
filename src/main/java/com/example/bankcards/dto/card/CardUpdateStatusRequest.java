package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardUpdateStatusRequest {
    @NotBlank
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
}
