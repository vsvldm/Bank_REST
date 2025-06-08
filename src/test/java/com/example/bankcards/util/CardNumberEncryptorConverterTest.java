package com.example.bankcards.util;

import com.example.bankcards.exception.exception.EncryptionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardNumberEncryptorConverterTest {

    private static CardNumberEncryptorConverter converter;

    @BeforeAll
    static void setup() {
        converter = new CardNumberEncryptorConverter();
    }

    @Test
    void encryptionAndDecryption_RoundTrip_Success() {
        String original = "1234567812345678";
        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_NullInput_ThrowsException() {
        assertThrows(EncryptionException.class, () -> converter.convertToDatabaseColumn(null));
    }

    @Test
    void decrypt_InvalidBase64_ThrowsException() {
        assertThrows(EncryptionException.class, () -> converter.convertToEntityAttribute("invalid_base64!"));
    }

    @Test
    void decrypt_CorruptedCiphertext_ThrowsException() {
        String valid = converter.convertToDatabaseColumn("valid_card");
        String corrupted = valid.substring(0, valid.length() - 5);
        assertThrows(EncryptionException.class, () -> converter.convertToEntityAttribute(corrupted));
    }
}