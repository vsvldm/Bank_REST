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
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final Principal principal = new UsernamePasswordAuthenticationToken("user", "password");

    private User createTestUser() {
        User user = new User();
        user.setUsername("user");
        return user;
    }

    private Card createTestCard(User owner, CardStatus status) {
        Card card = new Card();
        card.setId(1L);
        card.setOwner(owner);
        card.setStatus(status);
        card.setBalance(BigDecimal.valueOf(1000));
        return card;
    }

    @Test
    void createTransaction_Success() {
        User user = createTestUser();
        Card sourceCard = createTestCard(user, CardStatus.ACTIVE);
        Card targetCard = createTestCard(user, CardStatus.ACTIVE);

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(100));
        Transaction transaction = Transaction.builder()
                .sourceCard(sourceCard)
                .targetCard(targetCard)
                .status(TransactionStatus.PENDING)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(targetCard));
        when(transactionMapper.toTransaction(request, sourceCard, targetCard)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction, "Transaction in processing"))
                .thenReturn(new TransactionResponse(TransactionStatus.PENDING, "Transaction in processing"));

        TransactionResponse response = transactionService.createTransaction(principal, request);

        assertEquals(TransactionStatus.PENDING, response.getStatus());
        assertEquals("Transaction in processing", response.getMessage());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void createTransaction_CardBlocked_Failed() {
        User user = createTestUser();
        Card blockedCard = createTestCard(user, CardStatus.BLOCKED);
        Card activeCard = createTestCard(user, CardStatus.ACTIVE);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(activeCard));

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.TEN);
        Transaction transaction = Transaction.builder()
                .sourceCard(blockedCard)
                .targetCard(activeCard)
                .amount(BigDecimal.TEN)
                .status(TransactionStatus.FAILED)
                .build();

        when(transactionMapper.toTransaction(request, blockedCard, activeCard)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction, "Cannot use blocked card"))
                .thenReturn(new TransactionResponse(TransactionStatus.FAILED, "Cannot use blocked card"));

        TransactionResponse response = transactionService.createTransaction(principal, request);

        assertEquals(TransactionStatus.FAILED, response.getStatus());
        assertEquals("Cannot use blocked card", response.getMessage());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void createTransaction_SaveFails_ThrowsCreationException() {
        User user = createTestUser();
        Card sourceCard = createTestCard(user, CardStatus.ACTIVE);
        Card targetCard = createTestCard(user, CardStatus.ACTIVE);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(targetCard));
        when(transactionRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.TEN);

        CreationException exception = assertThrows(CreationException.class,
                () -> transactionService.createTransaction(principal, request));
        assertTrue(exception.getMessage().contains("DB error"));
    }

    @Test
    void updateStatusTransaction_Success() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(TransactionStatus.SUCCESS);
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction, "Status changed"))
                .thenReturn(new TransactionResponse(TransactionStatus.SUCCESS, "Status changed"));

        TransactionResponse response = transactionService.updateStatusTransaction(1L, updateRequest);

        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals("Status changed", response.getMessage());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void getTransactions_WithFilters_Success() {
        User user = createTestUser();
        Card filterCard = createTestCard(user, CardStatus.ACTIVE);
        Transaction transaction = new Transaction();
        transaction.setSourceCard(filterCard);
        transaction.setStatus(TransactionStatus.PENDING);

        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(filterCard));
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toFullResponse(any())).thenReturn(new TransactionFullResponse());

        List<TransactionFullResponse> result = transactionService.getTransactions(
                1L,
                TransactionStatus.PENDING,
                PageRequest.of(0, 10)
        );

        assertFalse(result.isEmpty());
        verify(transactionRepository).findAll(any(Pageable.class));
    }

    @Test
    void getTransactionsByCard_ValidCard_ReturnsTransactions() {
        User user = createTestUser();
        Card card = createTestCard(user, CardStatus.ACTIVE);
        Transaction transaction = new Transaction();
        transaction.setSourceCard(card);
        transaction.setStatus(TransactionStatus.SUCCESS);

        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toFullResponse(any())).thenReturn(new TransactionFullResponse());

        List<TransactionFullResponse> result = transactionService.getTransactionsByCard(
                1L,
                TransactionStatus.SUCCESS,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.size());
        verify(cardRepository).findById(1L);
    }
}