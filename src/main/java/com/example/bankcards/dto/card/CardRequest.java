package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardRequest {
    @NotBlank(message = "The card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "The card number must contain 16 digits")
    private String cardNumber;

    @NotNull(message = "The date of expiration is required")
    @Future(message = "The date of expiration should be in the future")
    private LocalDate expirationDate;
}
