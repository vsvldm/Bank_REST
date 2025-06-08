package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardMaskingUtilTest {

    @Test
    void mask_Valid16DigitCard_ReturnsMasked() {
        String result = CardMaskingUtil.mask("1234567812345678");
        assertEquals("**** **** **** 5678", result);
    }

    @Test
    void mask_ShortCard_ReturnsOriginal() {
        String input = "1234";
        String result = CardMaskingUtil.mask(input);
        assertSame(input, result);
    }

    @Test
    void mask_NullInput_ReturnsNull() {
        assertNull(CardMaskingUtil.mask(null));
    }

    @Test
    void mask_EmptyString_ReturnsEmpty() {
        assertEquals("", CardMaskingUtil.mask(""));
    }
}