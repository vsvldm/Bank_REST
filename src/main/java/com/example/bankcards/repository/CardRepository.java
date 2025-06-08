package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Card> findByCardNumber(String cardNumber);

    Page<Card> findByStatusAndOwner(CardStatus status, User owner, Pageable pageable);

    Page<Card> findByOwner(User owner, Pageable pageable);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);
}
