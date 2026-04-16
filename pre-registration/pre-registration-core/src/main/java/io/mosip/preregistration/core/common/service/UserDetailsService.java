package io.mosip.preregistration.core.common.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;
import io.mosip.preregistration.core.util.CryptoUtil;

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
        if (identifier == null) {
            return null;
        }
        return identifier.trim().toLowerCase();
    }

    private boolean isUuid(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(identifier.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
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

    /**
     * Finds a canonical user row by normalized identifier hash.
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

    public Optional<String> resolveUserUuid(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            LOGGER.debug("Skipping user UUID resolution because identifier is blank");
            return Optional.empty();
        }
        String trimmedIdentifier = identifier.trim();
        if (isUuid(trimmedIdentifier)) {
            LOGGER.debug("Using identifier as user UUID because it is already a UUID");
            return Optional.of(trimmedIdentifier);
        }
        try {
            Optional<UserDetails> existingUser = findByIdentifier(trimmedIdentifier);
            if (existingUser.isPresent()) {
                LOGGER.debug("Resolved existing user UUID for masked identifier {}",
                        maskIdentifier(trimmedIdentifier));
                return Optional.of(existingUser.get().getUserId().toString());
            }

            UserDetails mappedUser = findOrCreateByIdentifier(trimmedIdentifier);
            LOGGER.debug("Resolved user UUID for masked identifier {}", maskIdentifier(trimmedIdentifier));
            return Optional.of(mappedUser.getUserId().toString());
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve user UUID for masked identifier {}", maskIdentifier(trimmedIdentifier), ex);
        }
        return Optional.empty();
    }

    public String resolveUserUuidOrIdentifier(String identifier) {
        return resolveUserUuid(identifier).orElseGet(() -> {
            String fallbackIdentifier = identifier == null ? "" : identifier.trim();
            if (!fallbackIdentifier.isEmpty()) {
                LOGGER.warn("Falling back to non-UUID identifier for masked user {}", maskIdentifier(fallbackIdentifier));
            }
            return fallbackIdentifier;
        });
    }

    public String maskIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        String trimmed = value.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex > 0 && atIndex < trimmed.length() - 1) {
            String local = trimmed.substring(0, atIndex);
            String domain = trimmed.substring(atIndex);
            return local.substring(0, 1) + "***" + domain;
        }
        if (trimmed.matches("\\+?\\d{10,12}")) {
            boolean hasPlus = trimmed.startsWith("+");
            String digits = hasPlus ? trimmed.substring(1) : trimmed;
            if (digits.length() <= 4) {
                return (hasPlus ? "+" : "") + "****";
            }
            String masked = "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
            return (hasPlus ? "+" : "") + masked;
        }
        if (isUuid(trimmed)) {
            return "***" + trimmed.substring(trimmed.length() - 6);
        }
        int visible = Math.min(4, trimmed.length());
        return "***" + trimmed.substring(trimmed.length() - visible);
    }

    public List<String> getUserLookupIds(String authUserId, boolean piiBackwardCompatibility) {
        Set<String> ids = new LinkedHashSet<>();
        Optional<String> userUuid = resolveUserUuid(authUserId);
        String trimmedAuthUserId = authUserId == null ? "" : authUserId.trim();
        userUuid.ifPresent(ids::add);
        if (piiBackwardCompatibility && !trimmedAuthUserId.isEmpty()) {
            ids.add(trimmedAuthUserId);
        }
        return new ArrayList<>(ids);
    }

    public boolean matchesUser(String authUserId, String storedUserId, boolean piiBackwardCompatibility) {
        String trimmedAuthUserId = authUserId == null ? "" : authUserId.trim();
        String trimmedStoredUserId = storedUserId == null ? "" : storedUserId.trim();
        Optional<String> authUserUuid = resolveUserUuid(authUserId);
        if (!piiBackwardCompatibility) {
            return authUserUuid.filter(trimmedStoredUserId::equals).isPresent();
        }
        if (authUserUuid.filter(trimmedStoredUserId::equals).isPresent()) {
            return true;
        }
        Optional<String> storedUserUuid = resolveUserUuid(trimmedStoredUserId);
        if (authUserUuid.isPresent() && storedUserUuid.isPresent()
                && authUserUuid.get().equals(storedUserUuid.get())) {
            return true;
        }
        return trimmedAuthUserId.equals(trimmedStoredUserId);
    }

    /**
     * Find or create a canonical user row for given identifier. This is idempotent.
     */
    @Transactional
    @CachePut(value = "user-details-cache", key = "#identifier.toLowerCase().trim()", condition = "#identifier != null")
    public UserDetails findOrCreateByIdentifier(String identifier) {
        String norm = normalize(identifier);
        if (norm == null || norm.isBlank()) {
            throw new IllegalArgumentException("identifier is required");
        }
        // Hashing the normalized identifier gives us the deterministic lookup key for user_details.
        // We still do not persist anything unless encryption succeeds, so we avoid hash-only rows.
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
                String encrypted = encryptIdentifierRequired(norm);
                existing.setIdentifierEncrypted(encrypted);
                existing.setEncryptedDtimes(LocalDateTime.now());
                needsSave = true;
                LOGGER.info("Repaired missing encrypted identifier for existing canonical user {}", existing.getUserId());
            }
            if (!needsSave) {
                return existing;
            }
            try {
                return userDetailsRepository.save(existing);
            } catch (DataIntegrityViolationException ex) {
                return userDetailsRepository.findByIdentifierHash(hash).orElseThrow(() -> ex);
            }
        }
        UserDetails u = new UserDetails();
        u.setUserId(UUID.randomUUID());
        u.setIdentifierHash(hash);
        u.setCrDtimes(LocalDateTime.now());
        String encrypted = encryptIdentifierRequired(norm);
        u.setIdentifierEncrypted(encrypted);
        u.setEncryptedDtimes(LocalDateTime.now());
        LOGGER.info("Creating new canonical user mapping for masked identifier {}", maskIdentifier(norm));
        try {
            return userDetailsRepository.save(u);
        } catch (DataIntegrityViolationException ex) {
            return userDetailsRepository.findByIdentifierHash(hash).orElseThrow(() -> ex);
        }
    }

}

