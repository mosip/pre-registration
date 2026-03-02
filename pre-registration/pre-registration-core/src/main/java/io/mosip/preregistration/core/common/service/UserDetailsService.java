package io.mosip.preregistration.core.common.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;
import io.mosip.preregistration.core.util.CryptoUtil;

/**
 * Service to manage canonical user registry: hashing identifier, optional encryption, and find-or-create.
 */
@Service
public class UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);

    private final UserDetailsRepository userDetailsRepository;
    private final CryptoUtil cryptoUtil;

    public UserDetailsService(UserDetailsRepository userDetailsRepository, CryptoUtil cryptoUtil) {
        this.userDetailsRepository = userDetailsRepository;
        this.cryptoUtil = cryptoUtil;
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
        if (plain == null || plain.isBlank()) {
            return null;
        }
        try {
            byte[] encrypted = cryptoUtil.encrypt(plain.getBytes(StandardCharsets.UTF_8), LocalDateTime.now());
            if (encrypted == null || encrypted.length == 0) {
                return null;
            }
            return new String(encrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.warn("Encryption failed - storing null encrypted value", ex);
            return null;
        }
    }

    private Optional<String> decryptIdentifierIfConfigured(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return Optional.empty();
        }
        try {
            byte[] plain = cryptoUtil.decrypt(encryptedValue.getBytes(StandardCharsets.UTF_8), LocalDateTime.now());
            if (plain == null || plain.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new String(plain, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            LOGGER.warn("Decryption failed for identifier_encrypted", ex);
            return Optional.empty();
        }
    }

    /**
     * Find canonical user by identifier (email/username/whatever) — uses normalized sha256.
     */
    @Cacheable(value = "user-details-cache", key = "#identifier.toLowerCase().trim()", condition = "#identifier != null")
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
    @CachePut(value = "user-details-cache", key = "#identifier.toLowerCase().trim()", condition = "#identifier != null")
    public UserDetails findOrCreateByIdentifier(String identifier) {
        String norm = normalize(identifier);
        if (norm == null) {
            throw new IllegalArgumentException("identifier is required");
        }
        String hash = sha256Hex(norm);
        Optional<UserDetails> found = userDetailsRepository.findByIdentifierHash(hash);
        if (found.isPresent()) {
            UserDetails existing = found.get();
            boolean needsSave = false;
            if (existing.getCrDtimes() == null) {
                existing.setCrDtimes(LocalDateTime.now());
                needsSave = true;
            }
            if (existing.getIdentifierEncrypted() == null || existing.getIdentifierEncrypted().isBlank()) {
                String encrypted = encryptIdentifierIfConfigured(identifier);
                if (encrypted != null) {
                    existing.setIdentifierEncrypted(encrypted);
                    existing.setEncryptedDtimes(LocalDateTime.now());
                    needsSave = true;
                }
            }
            if (needsSave) {
                return userDetailsRepository.save(existing);
            }
            return existing;
        }
        UserDetails u = new UserDetails();
        u.setUserId(UUID.randomUUID());
        u.setIdentifierHash(hash);
        u.setCrDtimes(LocalDateTime.now());
        String encrypted = encryptIdentifierIfConfigured(identifier);
        u.setIdentifierEncrypted(encrypted);
        if (encrypted != null && !encrypted.isBlank()) {
            u.setEncryptedDtimes(LocalDateTime.now());
        }
        return userDetailsRepository.save(u);
    }

    /**
     * Returns decrypted identifier for a canonical user id when encryption key is configured.
     */
    public Optional<String> getDecryptedIdentifier(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        Optional<UserDetails> user = userDetailsRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return decryptIdentifierIfConfigured(user.get().getIdentifierEncrypted());
    }
}
