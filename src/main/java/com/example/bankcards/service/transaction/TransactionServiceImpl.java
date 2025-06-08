package com.example.bankcards.service.transaction;

import com.example.bankcards.dto.transaction.TransactionFullResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.dto.transaction.TransactionUpdateRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transaction.Transaction;
import com.example.bankcards.entity.transaction.TransactionStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponse createTransaction(Principal principal, TransactionRequest request) {
        log.info("Creating transaction for user: {}", principal.getName());
        log.debug("Transaction request: {}", request);

        User user = findUserByName(principal.getName());
        log.debug("Found user: {}", user.getUsername());

        Card sourceCard = getCardById(request.getSourceCardId());
        Card targetCard = getCardById(request.getTargetCardId());
        log.debug("Source card: {}, Target card: {}", sourceCard.getId(), targetCard.getId());

        if (!sourceCard.getOwner().equals(user) || !targetCard.getOwner().equals(user)) {
            log.warn("Transaction rejected - cards don't belong to user: {}", user.getUsername());
            throw new BadRequestException("Transaction can only be made between your cards");
        }

        Transaction transaction = transactionMapper.toTransaction(request, sourceCard, targetCard);
        log.debug("Created transaction entity: {}", transaction);

        String message;
        if (sourceCard.getStatus() == CardStatus.BLOCKED || targetCard.getStatus() == CardStatus.BLOCKED) {
            transaction.setStatus(TransactionStatus.FAILED);
            message = "Cannot use blocked card";
            log.warn("Transaction failed - card blocked: {}",
                    sourceCard.getStatus() == CardStatus.BLOCKED ? "source" : "target");
        } else if (sourceCard.getStatus() == CardStatus.EXPIRED || targetCard.getStatus() == CardStatus.EXPIRED) {
            transaction.setStatus(TransactionStatus.FAILED);
            message = "Cannot use expired card";
            log.warn("Transaction failed - card expired: {}",
                    sourceCard.getStatus() == CardStatus.EXPIRED ? "source" : "target");
        } else if (sourceCard.getBalance().compareTo(request.getAmount()) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            message = "Insufficient funds";
            log.warn("Transaction failed - insufficient funds. Balance: {}, Amount: {}",
                    sourceCard.getBalance(), request.getAmount());
        } else {
            message = "Transaction in processing";
            log.info("Transaction processing started");
        }
        try {
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction saved with ID: {}", savedTransaction.getId());

            TransactionResponse response = transactionMapper.toTransactionResponse(savedTransaction, message);
            log.debug("Prepared response: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Failed to create transaction: {}", e.getMessage());
            throw new CreationException(String.format("Failed to create transaction: %s", e.getMessage()));
        }
    }

    @Override
    @Transactional
    public TransactionResponse updateStatusTransaction(Long transactionId, TransactionUpdateRequest request) {
        log.info("Updating transaction status. ID: {}, New status: {}", transactionId, request.getStatus());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found: ID {}", transactionId);
                    return new NotFoundException(String.format("Transaction with id=%d not found", transactionId));
                });

        if (transaction.getStatus() == request.getStatus()) {
            log.warn("Status update rejected - same status: {}", request.getStatus());
            throw new BadRequestException(String.format("Transaction status is already %s", transaction.getStatus()));
        }

        log.debug("Current status: {}, New status: {}", transaction.getStatus(), request.getStatus());
        transaction.setStatus(request.getStatus());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction status updated. ID: {}, Status: {}", transactionId, request.getStatus());

        String message = "Status changed";

        TransactionResponse response = transactionMapper.toTransactionResponse(updatedTransaction, message);
        log.debug("Prepared response: {}", response);

        return response;
    }

    @Override
    public List<TransactionFullResponse> getTransactions(Long sourceCardId, TransactionStatus status, Pageable pageable) {
        log.info("Fetching transactions. Source card: {}, Status: {}, Page: {}",
                sourceCardId, status, pageable.getPageNumber());

        Card filterCard = sourceCardId != null ? getCardById(sourceCardId) : null;
        if (filterCard != null) {
            log.debug("Filtering by card: {}", filterCard.getId());
        }

        return filterAndMapTransactions(
                transactionRepository.findAll(pageable),
                filterCard,
                status
        );
    }

    @Override
    public List<TransactionFullResponse> getTransactionsByCard(Long cardId, TransactionStatus status, Pageable pageable) {
        log.info("Fetching transactions for card: {}, Status: {}", cardId, status);
        Card card = getCardById(cardId);
        return filterAndMapTransactions(
                transactionRepository.findAll(pageable),
                card,
                status
        );
    }

    private User findUserByName(String username) {
        log.debug("Looking for user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException(String.format("User %s not found", username));
                });
    }

    private Card getCardById(Long cardId) {
        log.debug("Looking for card: {}", cardId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found: {}", cardId);
                    return new NotFoundException(String.format("Card with id=%d not found", cardId));
                });
    }

    private List<TransactionFullResponse> filterAndMapTransactions(Page<Transaction> page, Card filterCard, TransactionStatus status) {
        log.debug("Filtering transactions. Card filter: {}, Status filter: {}",
                filterCard != null ? filterCard.getId() : "none",
                status);

        List<TransactionFullResponse> result = page.stream()
                .filter(t -> filterCard == null || (t.getSourceCard() != null && t.getSourceCard().equals(filterCard)))
                .filter(t -> status == null || t.getStatus() == status)
                .map(this::mapToFullResponse)
                .toList();

        log.info("Found {} transactions", result.size());
        return result;
    }

    private TransactionFullResponse mapToFullResponse(Transaction transaction) {
        TransactionFullResponse response = transactionMapper.toFullResponse(transaction);
        log.trace("Mapped transaction to full response: {}", response);
        return response;
    }
}