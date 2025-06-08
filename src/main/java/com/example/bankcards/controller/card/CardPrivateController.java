package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionFullResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.transaction.TransactionStatus;
import com.example.bankcards.service.card.CardService;
import com.example.bankcards.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "CardPrivateController", description = "Операции пользователя с картами")
@SecurityRequirement(name = "JWT")
public class CardPrivateController {
    private final CardService cardService;
    private final TransactionService transactionService;

    @Operation(
            summary = "Создать новую карту",
            description = "Создание банковской карты для текущего пользователя. Требует роли USER"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные карты"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(
            @Parameter(hidden = true) Principal principal,
            @RequestBody @Valid CardRequest cardRequest) {
        return cardService.create(principal, cardRequest);
    }

    @Operation(
            summary = "Удалить карту пользователя",
            description = "Удаление собственной карты. Требует роли USER",
            parameters = @Parameter(name = "cardId", description = "ID карты", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnerCard(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long cardId) {
        cardService.deleteOwnerCard(principal, cardId);
    }

    @Operation(
            summary = "Получить карты пользователя",
            description = "Список карт текущего пользователя с пагинацией. Требует роли USER",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "balance,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping
    public List<CardResponse> getCardsByOwner(
            @Parameter(hidden = true) Principal principal,
            @ParameterObject @PageableDefault(
                    sort = "balance",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return cardService.getCardsByOwner(principal, pageable);
    }

    @Operation(
            summary = "Создать транзакцию",
            description = "Перевод средств между картами. Требует роли USER"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Транзакция создана"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры перевода"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @Parameter(hidden = true) Principal principal,
            @RequestBody @Valid TransactionRequest request) {
        return transactionService.createTransaction(principal, request);
    }

    @Operation(
            summary = "Получить транзакции по карте",
            description = "История операций по конкретной карте с фильтром по статусу. Требует роли USER",
            parameters = {
                    @Parameter(name = "cardId", description = "ID карты", example = "1", in = ParameterIn.PATH),
                    @Parameter(name = "status", description = "Статус транзакции", example = "SUCCESS", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "timestamp,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/transactions/{cardId}")
    public List<TransactionFullResponse> getTransactionsByCard(
            @PathVariable Long cardId,
            @RequestParam TransactionStatus status,
            @ParameterObject @PageableDefault(
                    sort = "timestamp",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return transactionService.getTransactionsByCard(cardId, status, pageable);
    }
}