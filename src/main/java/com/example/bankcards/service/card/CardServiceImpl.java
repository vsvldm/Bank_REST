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
import com.example.bankcards.util.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponse create(Principal principal, CardRequest cardRequest) {
        log.info("Creating new card for user: {}", principal.getName());
        log.debug("Card request: {}", cardRequest);

        User owner = findUserByName(principal.getName());
        log.debug("Found owner: {}", owner.getUsername());

        Card card = cardMapper.toCard(cardRequest, owner);
        log.debug("Mapped to card entity");

        try {
            card = cardRepository.save(card);
            log.info("Card created successfully. ID: {}, Masked: {}",
                    card.getId(),
                    CardMaskingUtil.mask(card.getCardNumber()));
        } catch (Exception e) {
            log.error("Failed to create card: {}", e.getMessage());
            throw new CreationException(String.format("Failed to create card: %s", e.getMessage()));
        }

        CardResponse response = cardMapper.toCardResponse(card);
        log.debug("Prepared response");

        return response;
    }

    @Override
    @Transactional
    public void deleteOwnerCard(Principal principal, Long cardId) {
        log.info("User {} deleting card ID: {}", principal.getName(), cardId);

        User user = findUserByName(principal.getName());
        log.debug("User ID: {}", user.getId());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found: ID {}", cardId);
                    return new NotFoundException(String.format("Card with id=%d not found", cardId));
                });
        log.debug("Found card: {}", CardMaskingUtil.mask(card.getCardNumber()));

        if (!card.getOwner().equals(user)) {
            log.warn("User {} is not owner of card {}. Actual owner: {}",
                    user.getId(),
                    cardId,
                    card.getOwner().getId());
            throw new BadRequestException(String.format(
                    "The user id=%d is not the owner of the card with id=%d.",
                    user.getId(),
                    cardId));
        }

        cardRepository.delete(card);
        log.info("Card deleted successfully. ID: {}", cardId);
    }

    @Override
    public List<CardResponse> getCardsByOwner(Principal principal, Pageable pageable) {
        log.info("Fetching cards for owner: {}, page: {}", principal.getName(), pageable.getPageNumber());

        User user = findUserByName(principal.getName());
        log.debug("User ID: {}", user.getId());

        List<CardResponse> cards = cardRepository.findByOwnerId(user.getId(), pageable).stream()
                .map(cardMapper::toCardResponse)
                .toList();

        log.info("Found {} cards for owner {}", cards.size(), user.getUsername());
        return cards;
    }

    @Override
    public List<CardResponse> getCards(CardStatus status, String username, Pageable pageable) {
        log.info("Fetching cards. Status: {}, Username: {}, Page: {}",
                status, username, pageable.getPageNumber());

        if (username != null) {
            User user = findUserByName(username);
            log.debug("Filtering by user: {}", user.getUsername());

            List<CardResponse> cards;
            if (status != null) {
                cards = cardRepository.findByStatusAndOwner(status, user, pageable)
                        .stream()
                        .map(cardMapper::toCardResponse)
                        .toList();
                log.debug("Filtered by status and owner");
            } else {
                cards = cardRepository.findByOwner(user, pageable)
                        .stream()
                        .map(cardMapper::toCardResponse)
                        .toList();
                log.debug("Filtered by owner only");
            }

            log.info("Found {} cards for user {}", cards.size(), username);
            return cards;
        } else {
            List<CardResponse> cards;
            if (status != null) {
                cards = cardRepository.findByStatus(status, pageable)
                        .stream()
                        .map(cardMapper::toCardResponse)
                        .toList();
                log.debug("Filtered by status only");
            } else {
                cards = cardRepository.findAll(pageable)
                        .stream()
                        .map(cardMapper::toCardResponse)
                        .toList();
                log.debug("No filters applied");
            }

            log.info("Found {} cards", cards.size());
            return cards;
        }
    }

    @Override
    @Transactional
    public void updateStatus(CardUpdateStatusRequest updateStatusRequest) {
        String maskedNumber = CardMaskingUtil.mask(updateStatusRequest.getCardNumber());
        log.info("Updating card status. Card: {}, New status: {}",
                maskedNumber, updateStatusRequest.getStatus());

        Card card = cardRepository.findByCardNumber(updateStatusRequest.getCardNumber())
                .orElseThrow(() -> {
                    log.error("Card not found: {}", maskedNumber);
                    return new NotFoundException(String.format("Card %s not found", maskedNumber));
                });

        log.debug("Found card ID: {}, Current status: {}", card.getId(), card.getStatus());

        switch (updateStatusRequest.getStatus()) {
            case ACTIVE: {
                if (card.getStatus() == CardStatus.ACTIVE) {
                    log.warn("Card already active. ID: {}", card.getId());
                    throw new BadRequestException(String.format(
                            "Card with id=%d is already active",
                            card.getId()));
                }
                if (card.getStatus() == CardStatus.EXPIRED) {
                    log.warn("Cannot activate expired card. ID: {}", card.getId());
                    throw new BadRequestException(String.format(
                            "Card with id=%d is expired",
                            card.getId()));
                }
                card.setStatus(CardStatus.ACTIVE);
                log.info("Card activated. ID: {}", card.getId());
                break;
            }
            case BLOCKED: {
                if (card.getStatus() == CardStatus.BLOCKED) {
                    log.warn("Card already blocked. ID: {}", card.getId());
                    throw new BadRequestException(String.format(
                            "Card with id=%d is blocked",
                            card.getId()));
                }
                if (card.getStatus() == CardStatus.EXPIRED) {
                    log.warn("Cannot block expired card. ID: {}", card.getId());
                    throw new BadRequestException(String.format(
                            "Card with id=%d is expired",
                            card.getId()));
                }
                card.setStatus(CardStatus.BLOCKED);
                log.info("Card blocked. ID: {}", card.getId());
                break;
            }
            default: {
                log.error("Invalid status requested: {}", updateStatusRequest.getStatus());
                throw new BadRequestException(String.format(
                        "Status %s does not exist",
                        updateStatusRequest.getStatus()));
            }
        }

        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void delete(Long cardId) {
        log.info("Deleting card ID: {}", cardId);

        if (!cardRepository.existsById(cardId)) {
            log.error("Card not found: ID {}", cardId);
            throw new NotFoundException(String.format("Card with id=%d not found.", cardId));
        }

        cardRepository.deleteById(cardId);
        log.info("Card deleted successfully. ID: {}", cardId);
    }

    private User findUserByName(String username) {
        log.debug("Looking for user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException(String.format("User %s not found", username));
                });
    }
}
