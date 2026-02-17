package io.mosip.preregistration.core.common.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;

/**
 * Service to manage canonical user registry: hashing identifier, optional encryption, and find-or-create.
 */
@Service
public class UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);

    private final UserDetailsRepository userDetailsRepository;

    @Value("${prereg.user.encryption.key:}")
    private String encryptionKey;

    public UserDetailsService(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    private String normalize(String identifier) {
        if (identifier == null) {
            return null;
        }
        return identifier.trim().toLowerCase();
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to compute SHA-256 hash", ex);
        }
    }

    private String encryptIdentifierIfConfigured(String plain) {
        if (plain == null || plain.isBlank() || encryptionKey == null || encryptionKey.isBlank()) {
            return null;
        }
        try {
            // Simple AES-GCM placeholder. Use proper key management in production.
            // Construct a simple IV + base64 ciphertext encoding to store in DB.
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(keyBytes, 0, Math.min(32, keyBytes.length), "AES");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText);
            return encoded;
        } catch (Exception ex) {
            LOGGER.warn("Encryption failed - storing null encrypted value", ex);
            return null;
        }
    }

    /**
     * Find canonical user by identifier (email/username/whatever) — uses normalized sha256.
     */
    @Cacheable(value = "user-details-cache", key = "#identifier == null ? null : #identifier.toLowerCase().trim()")
    public Optional<UserDetails> findByIdentifier(String identifier) {
        String norm = normalize(identifier);
        if (norm == null) {
            return Optional.empty();
        }
        String hash = sha256Hex(norm);
        return userDetailsRepository.findByIdentifierHash(hash);
    }

    /**
     * Find or create a canonical user row for given identifier. This is idempotent.
     */
    @Transactional
    @CachePut(value = "user-details-cache", key = "#identifier == null ? null : #identifier.toLowerCase().trim()")
    public UserDetails findOrCreateByIdentifier(String identifier) {
        String norm = normalize(identifier);
        if (norm == null) {
            throw new IllegalArgumentException("identifier is required");
        }
        String hash = sha256Hex(norm);
        Optional<UserDetails> found = userDetailsRepository.findByIdentifierHash(hash);
        if (found.isPresent()) {
            return found.get();
        }
        UserDetails u = new UserDetails();
        u.setUserId(UUID.randomUUID());
        u.setIdentifierHash(hash);
        u.setIdentifierEncrypted(encryptIdentifierIfConfigured(identifier));
        return userDetailsRepository.save(u);
    }
}
