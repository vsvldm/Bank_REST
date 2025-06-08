package com.example.bankcards.entity.transaction;

import com.example.bankcards.entity.card.Card;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_card_id", nullable = false)
    private Card sourceCard;

    @ManyToOne
    @JoinColumn(name = "target_card_id", nullable = false)
    private Card targetCard;

    @JoinColumn(name = "amount", nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @JoinColumn(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "status", nullable = false)
    private TransactionStatus status;

    @PrePersist
    private void setDefaultStatus() {
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
}
