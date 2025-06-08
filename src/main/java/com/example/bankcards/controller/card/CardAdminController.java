package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateStatusRequest;
import com.example.bankcards.dto.transaction.TransactionFullResponse;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.dto.transaction.TransactionUpdateRequest;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transaction.TransactionStatus;
import com.example.bankcards.service.card.CardService;
import com.example.bankcards.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@Tag(name = "CardAdminController", description = "Административные операции с картами")
@SecurityRequirement(name = "JWT")
public class CardAdminController {
    private final CardService cardService;
    private final TransactionService transactionService;

    @Operation(
            summary = "Получить список карт",
            description = "Фильтрация по статусу и владельцу с пагинацией. Требует роли ADMIN",
            parameters = {
                    @Parameter(name = "status", description = "Статус карты", example = "ACTIVE", in = ParameterIn.QUERY),
                    @Parameter(name = "username", description = "Имя пользователя", example = "user123", in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "balance,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content)
    })
    @GetMapping
    public List<CardResponse> getCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String username,
            @ParameterObject @PageableDefault(
                    sort = "balance",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return cardService.getCards(status, username, pageable);
    }

    @Operation(
            summary = "Обновить статус карты",
            description = "Модерация статуса карты (например, блокировка). Требует роли ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Статус обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PatchMapping("/moderation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@RequestBody @Valid CardUpdateStatusRequest updateStatusRequest) {
        cardService.updateStatus(updateStatusRequest);
    }

    @Operation(
            summary = "Удалить карту",
            description = "Полное удаление карты из системы. Требует роли ADMIN",
            parameters = @Parameter(name = "cardId", description = "ID карты", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long cardId) {
        cardService.delete(cardId);
    }

    @Operation(
            summary = "Обновить статус транзакции",
            description = "Изменение статуса финансовой операции. Требует роли ADMIN",
            parameters = @Parameter(name = "transactionId", description = "ID транзакции", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    })
    @PatchMapping("/transactions/{transactionId}")
    public TransactionResponse updateStatusTransaction(@PathVariable Long transactionId,
                                                       @RequestBody @Valid TransactionUpdateRequest transactionUpdateRequest) {
        return transactionService.updateStatusTransaction(transactionId, transactionUpdateRequest);
    }

    @Operation(
            summary = "Получить список транзакций",
            description = "Фильтрация по карте и статусу с пагинацией. Требует роли ADMIN",
            parameters = {
                    @Parameter(name = "sourceCardId", description = "ID исходной карты", example = "1", in = ParameterIn.QUERY),
                    @Parameter(name = "status", description = "Статус транзакции", example = "SUCCESS", in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "timestamp,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping("/transactions")
    public List<TransactionFullResponse> getTransactions(
            @RequestParam(required = false) Long sourceCardId,
            @RequestParam(required = false) TransactionStatus status,
            @ParameterObject @PageableDefault(
                    sort = "timestamp",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return transactionService.getTransactions(sourceCardId, status, pageable);
    }
}
