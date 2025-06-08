package com.example.bankcards.entity.card;

import com.example.bankcards.entity.user.User;
import com.example.bankcards.util.CardNumberEncryptorConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "card_number", nullable = false)
    @Convert(converter = CardNumberEncryptorConverter.class)
    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @JoinColumn(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @JoinColumn(name = "balance", nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "status")
    private CardStatus status;
}
