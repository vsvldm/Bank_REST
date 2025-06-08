package com.example.bankcards.repository;

import com.example.bankcards.entity.transaction.Transaction;
import com.example.bankcards.entity.transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Collection<Transaction> findByStatus(TransactionStatus status);
}
