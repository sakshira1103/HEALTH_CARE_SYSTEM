package com.healthcare.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA attribute converter applied to entity fields via @Convert(converter = AttributeEncryptor.class).
 * Any field annotated with it is transparently encrypted before INSERT/UPDATE
 * and transparently decrypted after SELECT — callers never see ciphertext.
 *
 * JPA instantiates converters itself (not Spring), so the encryption service is
 * wired in through a static holder set once at application startup by
 * {@link EncryptionServiceHolder}.
 */
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        EncryptionService service = EncryptionServiceHolder.getInstance();
        return service != null ? service.encrypt(attribute) : attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        EncryptionService service = EncryptionServiceHolder.getInstance();
        return service != null ? service.decrypt(dbData) : dbData;
    }

    /**
     * Bridges Spring's managed EncryptionService bean into the JPA-managed
     * converter instance, which Spring does not control directly.
     */
    @Component
    public static class EncryptionServiceHolder {
        private static EncryptionService instance;

        @Autowired
        public EncryptionServiceHolder(EncryptionService encryptionService) {
            instance = encryptionService;
        }

        public static EncryptionService getInstance() {
            return instance;
        }
    }
}
