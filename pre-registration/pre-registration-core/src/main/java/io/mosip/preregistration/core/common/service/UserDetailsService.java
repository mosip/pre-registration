package io.mosip.preregistration.core.common.service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * Service for canonical user identity handling: normalization, UUID resolution, lookup, masking,
 * and user-details registry management.
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
        return identifier.trim().toLowerCase();
    }

    private String sha256Hex(String input) {
        try {
            return HMACUtils2.digestAsPlainText(input.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unable to compute SHA-256 hash", ex);
        }
    }

    /**
     * Encrypts the given plain identifier. Returns null if input is blank, encryption fails, or produces empty output.
     * The null/blank guard is kept intentionally at this helper boundary so encryption failure remains controlled (null)
     * instead of degrading into an accidental NullPointerException if the method is ever reused with unchecked input.
     */
    private String encryptIdentifier(String plain) {
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
            LOGGER.warn("Encryption failed for user identifier");
            return null;
        }
    }

    /**
     * Encrypts and throws if the result is absent; encryption is mandatory before persisting.
     */
    private String encryptIdentifierRequired(String plain) {
        String encryptedValue = encryptIdentifier(plain);
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalStateException("Encrypted identifier is required before persisting user details");
        }
        return encryptedValue;
    }

    @Cacheable(value = "user-details-cache", key = "#identifier.toLowerCase().trim()", condition = "#identifier != null", unless = "#result == null")
    public String resolveUserUuid(String identifier) {
        try {
            String norm = normalize(identifier);
            String hash = sha256Hex(norm);
            UserDetails user = userDetailsRepository.findByIdentifierHash(hash)
                    .orElseGet(() -> createByIdentifier(identifier, norm, hash));
            LOGGER.debug("Resolved user UUID for masked identifier {}", GenericUtil.maskIdentifier(identifier));
            return user.getUserId().toString();
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve user UUID for masked identifier {}", GenericUtil.maskIdentifier(identifier), ex);
        }
        return null;
    }

    public List<String> getUserLookupIds(String authUserId, boolean piiBackwardCompatibility) {
        List<String> ids = new ArrayList<>();
        String userUuid = resolveUserUuid(authUserId);
        if (userUuid != null) {
            ids.add(userUuid);
        }
        if (piiBackwardCompatibility && !authUserId.isEmpty()) {
            ids.add(authUserId);
        }
        return ids;
    }

    public boolean matchesUser(String authUserId, String storedUserId, boolean piiBackwardCompatibility) {
        String trimmedAuthUserId = authUserId == null ? "" : authUserId.trim();
        String trimmedStoredUserId = storedUserId == null ? "" : storedUserId.trim();
        String authUserUuid = resolveUserUuid(authUserId);
        if (!piiBackwardCompatibility) {
            return trimmedStoredUserId.equals(authUserUuid);
        }
        if (authUserUuid != null && trimmedStoredUserId.equals(authUserUuid)) {
            return true;
        }
        String storedUserUuid = GenericUtil.isUuid(trimmedStoredUserId) ? trimmedStoredUserId : resolveUserUuid(trimmedStoredUserId);
        if (authUserUuid != null && storedUserUuid != null && authUserUuid.equals(storedUserUuid)) {
            return true;
        }
        return trimmedAuthUserId.equals(trimmedStoredUserId);
    }

    /**
     * Find or create a canonical user row for given identifier. This is idempotent.
     */
    @Transactional
    public UserDetails createByIdentifier(String raw, String norm, String hash) {
        UserDetails u = new UserDetails();
        u.setUserId(UUID.randomUUID());
        u.setIdentifierHash(hash);
        u.setCrDtimes(LocalDateTime.now());
        u.setIdentifierEncrypted(encryptIdentifierRequired(raw));
        u.setEncryptedDtimes(LocalDateTime.now());
        LOGGER.info("Creating new canonical user mapping for masked identifier {}", GenericUtil.maskIdentifier(norm));
        try {
            return userDetailsRepository.save(u);
        } catch (DataIntegrityViolationException ex) {
            return userDetailsRepository.findByIdentifierHash(hash).orElseThrow(() -> ex);
        }
    }

}

