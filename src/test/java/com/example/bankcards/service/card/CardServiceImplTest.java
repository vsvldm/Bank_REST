package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateStatusRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private CardMapper cardMapper;
    @Mock private Principal principal;

    @InjectMocks private CardServiceImpl cardService;

    @Captor private ArgumentCaptor<Card> cardCaptor;

    private User testUser;
    private Card testCard;
    private CardRequest cardRequest;
    private final LocalDate futureDate = LocalDate.now().plusYears(1);
    private final String cardNumber = "1234567890123456";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber(cardNumber);
        testCard.setOwner(testUser);
        testCard.setExpirationDate(futureDate);
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setStatus(CardStatus.ACTIVE);

        cardRequest = new CardRequest(cardNumber, futureDate);
    }

    @Test
    void createCard_ValidRequest_ShouldReturnResponse() {
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardMapper.toCard(cardRequest, testUser)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toCardResponse(testCard)).thenReturn(new CardResponse());

        CardResponse response = cardService.create(principal, cardRequest);

        assertNotNull(response);
        verify(cardRepository).save(testCard);
    }

    @Test
    void createCard_UserNotFound_ShouldThrowException() {
        when(principal.getName()).thenReturn("unknownUser");
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            cardService.create(principal, cardRequest);
        });
    }

    @Test
    void createCard_SaveFails_ShouldThrowCreationException() {
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardMapper.toCard(cardRequest, testUser)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenThrow(new RuntimeException("DB error"));

        assertThrows(CreationException.class, () -> {
            cardService.create(principal, cardRequest);
        });
    }

    @Test
    void deleteOwnerCard_ValidRequest_ShouldDeleteCard() {
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        cardService.deleteOwnerCard(principal, 1L);

        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteOwnerCard_NotCardOwner_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(principal.getName()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        assertThrows(BadRequestException.class, () -> {
            cardService.deleteOwnerCard(principal, 1L);
        });
    }

    @Test
    void deleteOwnerCard_CardNotFound_ShouldThrowException() {
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            cardService.deleteOwnerCard(principal, 1L);
        });
    }

    @Test
    void getCardsByOwner_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(Collections.singletonList(testCard));

        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByOwnerId(testUser.getId(), pageable)).thenReturn(page);
        when(cardMapper.toCardResponse(testCard)).thenReturn(new CardResponse());

        List<CardResponse> result = cardService.getCardsByOwner(principal, pageable);

        assertEquals(1, result.size());
        verify(cardRepository).findByOwnerId(testUser.getId(), pageable);
    }

    @Test
    void getCards_WithUsernameAndStatus_ShouldFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(Collections.singletonList(testCard));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByStatusAndOwner(eq(CardStatus.ACTIVE), eq(testUser), eq(pageable)))
                .thenReturn(page);
        when(cardMapper.toCardResponse(testCard)).thenReturn(new CardResponse());

        List<CardResponse> result = cardService.getCards(CardStatus.ACTIVE, "testUser", pageable);

        assertEquals(1, result.size());
        verify(cardRepository).findByStatusAndOwner(CardStatus.ACTIVE, testUser, pageable);
    }

    @Test
    void getCards_WithoutFilters_ShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(Collections.singletonList(testCard));

        when(cardRepository.findAll(pageable)).thenReturn(page);
        when(cardMapper.toCardResponse(testCard)).thenReturn(new CardResponse());

        List<CardResponse> result = cardService.getCards(null, null, pageable);

        assertEquals(1, result.size());
        verify(cardRepository).findAll(pageable);
    }

    @ParameterizedTest
    @EnumSource(value = CardStatus.class, names = {"ACTIVE", "BLOCKED"})
    void updateStatus_ValidTransition_ShouldUpdate(CardStatus newStatus) {
        testCard.setStatus(newStatus == CardStatus.ACTIVE ? CardStatus.BLOCKED : CardStatus.ACTIVE);
        CardUpdateStatusRequest request = new CardUpdateStatusRequest(cardNumber, newStatus);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        cardService.updateStatus(request);

        assertEquals(newStatus, testCard.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void updateStatus_ToExpired_ShouldThrowException() {
        CardUpdateStatusRequest request = new CardUpdateStatusRequest(cardNumber, CardStatus.EXPIRED);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        assertThrows(BadRequestException.class, () -> {
            cardService.updateStatus(request);
        });
    }

    @Test
    void updateStatus_AlreadyInStatus_ShouldThrowException() {
        testCard.setStatus(CardStatus.ACTIVE);
        CardUpdateStatusRequest request = new CardUpdateStatusRequest(cardNumber, CardStatus.ACTIVE);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        assertThrows(BadRequestException.class, () -> {
            cardService.updateStatus(request);
        });
    }

    @Test
    void updateStatus_ExpiredCard_ShouldThrowException() {
        testCard.setStatus(CardStatus.EXPIRED);
        CardUpdateStatusRequest request = new CardUpdateStatusRequest(cardNumber, CardStatus.ACTIVE);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        assertThrows(BadRequestException.class, () -> {
            cardService.updateStatus(request);
        });
    }

    @Test
    void updateStatus_CardNotFound_ShouldThrowException() {
        CardUpdateStatusRequest request = new CardUpdateStatusRequest("invalid", CardStatus.ACTIVE);

        when(cardRepository.findByCardNumber("invalid")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            cardService.updateStatus(request);
        });
    }

    @Test
    void deleteCard_ValidRequest_ShouldDelete() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.delete(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_NotExists_ShouldThrowException() {
        when(cardRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> {
            cardService.delete(1L);
        });
    }
}