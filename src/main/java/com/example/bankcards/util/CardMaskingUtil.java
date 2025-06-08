package com.example.bankcards.util;

public class CardMaskingUtil {
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) return cardNumber;
        return "**** **** **** " + cardNumber.substring(12);
    }
}