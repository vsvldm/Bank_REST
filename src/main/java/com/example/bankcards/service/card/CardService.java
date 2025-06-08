package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateStatusRequest;
import com.example.bankcards.entity.card.CardStatus;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface CardService {
    CardResponse create(Principal principal, CardRequest cardRequest);

    void deleteOwnerCard(Principal principal, Long cardId);

    List<CardResponse> getCardsByOwner(Principal principal, Pageable pageable);

    List<CardResponse> getCards(CardStatus status, String username, Pageable pageable);

    void updateStatus(CardUpdateStatusRequest updateStatusRequest);

    void delete(Long cardId);
}
