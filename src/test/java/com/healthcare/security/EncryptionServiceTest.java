package com.healthcare.security;

import com.healthcare.encryption.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies field-level encryption round-trips correctly, produces different
 * ciphertext for the same plaintext each time (random IV), and detects
 * tampering (GCM authentication tag).
 */
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String TEST_KEY = "dGhpc2lzYTMyYnl0ZWtleWZvcmRlbW9wdXJwb3Nlcw==";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService(TEST_KEY);
    }

    @Test
    void encryptThenDecrypt_returnsOriginalPlaintext() {
        String original = "Patient has type 2 diabetes, prescribed Metformin 500mg";
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        assertEquals(original, decrypted);
        assertNotEquals(original, encrypted, "Ciphertext should never equal plaintext");
    }

    @Test
    void sameInput_producesDifferentCiphertextEachTime() {
        String plain = "John Doe";
        String encrypted1 = encryptionService.encrypt(plain);
        String encrypted2 = encryptionService.encrypt(plain);

        assertNotEquals(encrypted1, encrypted2, "Random IV should make each encryption unique");
        assertEquals(plain, encryptionService.decrypt(encrypted1));
        assertEquals(plain, encryptionService.decrypt(encrypted2));
    }

    @Test
    void tamperedCiphertext_failsToDecrypt() {
        String encrypted = encryptionService.encrypt("sensitive diagnosis data");
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "abcd";

        assertThrows(EncryptionService.EncryptionException.class,
                () -> encryptionService.decrypt(tampered));
    }

    @Test
    void nullInput_returnsNull() {
        assertNull(encryptionService.encrypt(null));
        assertNull(encryptionService.decrypt(null));
    }
}
