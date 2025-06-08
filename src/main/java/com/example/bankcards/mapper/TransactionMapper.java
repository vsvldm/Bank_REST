    package com.example.bankcards.mapper;

    import com.example.bankcards.dto.transaction.TransactionRequest;
    import com.example.bankcards.dto.transaction.TransactionResponse;
    import com.example.bankcards.dto.transaction.TransactionFullResponse;
    import com.example.bankcards.entity.card.Card;
    import com.example.bankcards.entity.transaction.Transaction;
    import com.example.bankcards.util.CardMaskingUtil;
    import org.springframework.stereotype.Component;

    @Component
    public class TransactionMapper {
        public Transaction toTransaction(TransactionRequest request, Card sourceCard, Card targetCard) {
            return Transaction.builder()
                    .sourceCard(sourceCard)
                    .targetCard(targetCard)
                    .amount(request.getAmount())
                    .build();
        }

        public TransactionResponse toTransactionResponse(Transaction transaction, String message) {
            return TransactionResponse.builder()
                    .status(transaction.getStatus())
                    .message(message)
                    .build();
        }

        public TransactionFullResponse toFullResponse(Transaction transaction) {
            return TransactionFullResponse.builder()
                    .id(transaction.getId())
                    .sourceCardNumber(CardMaskingUtil.mask(transaction.getSourceCard().getCardNumber()))
                    .targetCardNumber(CardMaskingUtil.mask(transaction.getTargetCard().getCardNumber()))
                    .amount(transaction.getAmount())
                    .timestamp(transaction.getTimestamp())
                    .status(transaction.getStatus())
                    .build();
        }
    }