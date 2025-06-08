package com.example.bankcards.util;

import com.example.bankcards.exception.exception.EncryptionException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.KeySpec;
import java.util.Base64;

@Converter
public class CardNumberEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private static final String PASSWORD;
    private static final String SALT;

    private static final SecretKey SECRET_KEY;
    private static final IvParameterSpec IV;

    static {

        //TODO небезопасно
        PASSWORD = "uB7!kP2#qR9@zY5*eF3$jH6%mN1^wL4&";
        SALT = "sE7#rT2@kY9!pL5*";

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            KeySpec spec = new PBEKeySpec(PASSWORD.toCharArray(), SALT.getBytes(), ITERATIONS, KEY_LENGTH);
            SECRET_KEY = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            IV = new IvParameterSpec(new byte[16]);
        } catch (Exception e) {
            throw new EncryptionException("Failed to initialize encryption");
        }
    }

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, IV);
            return Base64.getEncoder().encodeToString(cipher.doFinal(cardNumber.getBytes()));
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed");
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, IV);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber)));
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed");
        }
    }
}