package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.util.CardMaskingUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class CardMapper {
    public Card toCard(CardRequest request, User owner) {
        return Card.builder()
                .cardNumber(request.getCardNumber())
                .owner(owner)
                .expirationDate(request.getExpirationDate())
                .balance(randomBalance()) //Затычка
                .status(CardStatus.ACTIVE)
                .build();
    }

    public CardResponse toCardResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(CardMaskingUtil.mask(card.getCardNumber()))
                .ownerName(card.getOwner().getUsername())
                .expirationDate(card.getExpirationDate().toString())
                .balance(card.getBalance())
                .status(card.getStatus())
                .build();
    }

    private BigDecimal randomBalance() {
        double randomValue = ThreadLocalRandom.current().nextDouble(0, 50000);
        return BigDecimal.valueOf(randomValue)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
