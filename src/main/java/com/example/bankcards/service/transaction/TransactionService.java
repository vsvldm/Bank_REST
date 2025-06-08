package com.example.bankcards.service.transaction;

import com.example.bankcards.dto.transaction.TransactionFullResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.dto.transaction.TransactionUpdateRequest;
import com.example.bankcards.entity.transaction.TransactionStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(Principal principal, TransactionRequest request);

    TransactionResponse updateStatusTransaction(Long transactionId, TransactionUpdateRequest transactionUpdateRequest);

    List<TransactionFullResponse> getTransactions(Long sourceCardId, TransactionStatus status, Pageable pageable);

    List<TransactionFullResponse> getTransactionsByCard(Long cardId, TransactionStatus status, Pageable pageable);
}
