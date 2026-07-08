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
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * Service for internal user identity handling: normalization, UUID resolution, lookup, masking,
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

    private String standardize(String userId) {
        if (userId == null) {
            return "";
        }
        return userId.trim().toLowerCase();
    }

    private String sha256Hex(String input) {
        try {
            // Force uppercase hex so the stored/looked-up hash is canonical regardless of the
            // hex-encoding case emitted by the underlying kernel util. Without this, a code path
            // producing lowercase hex (older MessageDigest/Integer.toHexString impl) and one
            // producing uppercase hex (HMACUtils2) create two rows for the same identifier,
            // because identifier_hash is compared case-sensitively.
            return HMACUtils2.digestAsPlainText(input.getBytes()).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to compute SHA-256 hash", ex);
        }
    }

    private String encryptUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            byte[] encryptedValue = cryptoUtil.encrypt(userId.getBytes(StandardCharsets.UTF_8), LocalDateTime.now());
            if (encryptedValue == null || encryptedValue.length == 0) {
                LOGGER.error("Empty encrypted value generated for masked user {}", GenericUtil.maskIdentifier(userId));
                return null;
            }
            return new String(encryptedValue, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.error("Failed encrypting userId for masked user {}", GenericUtil.maskIdentifier(userId), ex);
            return null;
        }
    }

    private String encryptUserIdRequired(String userId) {
        String encryptedValue = encryptUserId(userId);
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalStateException("Encrypted userId is required before persisting user details");
        }
        return encryptedValue;
    }

    @Cacheable(value = "user-details-cache", key = "#userId == null ? '' : #userId.toLowerCase().trim()", condition = "#userId != null")
    public String getOrCreateInternalUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            String standardizedUserId = standardize(userId);
            String userHash = sha256Hex(standardizedUserId);
            UserDetails userDetails = userDetailsRepository.findByIdentifierHash(userHash)
                    .orElseGet(() -> createInternalUser(userId, standardizedUserId, userHash));
            LOGGER.debug("Fetched internal user ID for masked user {}", GenericUtil.maskIdentifier(userId));
            return userDetails.getUserId().toString();
        } catch (UserLookupException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Failed fetching internal user ID for masked user {}", GenericUtil.maskIdentifier(userId), ex);
            throw new UserLookupException(ErrorCodes.PRG_CORE_REQ_024.getCode(),
                    ErrorMessages.USER_LOOKUP_FAILED.getMessage(), ex);
        }
    }

    public List<String> getUserLookupIds(String userId, boolean piiBackwardCompatibility) {
        List<String> lookupIds = new ArrayList<>();
        if (userId == null || userId.isBlank()) {
            return lookupIds;
        }
        String trimmedUserId = userId.trim();
        try {
            String internalUserId = getOrCreateInternalUserId(trimmedUserId);
            if (internalUserId != null && !internalUserId.isBlank()) {
                lookupIds.add(internalUserId);
            }
        } catch (UserLookupException ex) {
            LOGGER.warn("UUID resolution failed for lookup, piiBackwardCompatibility={}. maskedUser={}",
                    piiBackwardCompatibility, GenericUtil.maskIdentifier(userId));
        }
        if (piiBackwardCompatibility) {
            lookupIds.add(trimmedUserId);
        }
        return lookupIds.stream().distinct().toList();
    }

    public boolean matchesUser(String authUserId, String storedUserId, boolean piiBackwardCompatibility) {
        if (authUserId == null || authUserId.isBlank() || storedUserId == null || storedUserId.isBlank()) {
            return false;
        }
        String internalAuthUserId;
        try {
            internalAuthUserId = getOrCreateInternalUserId(authUserId);
        } catch (UserLookupException ex) {
            LOGGER.warn("UUID resolution failed during user match for masked user {}", GenericUtil.maskIdentifier(authUserId));
            return false;
        }
        if (internalAuthUserId == null) {
            return false;
        }
        // New records store internal UUID — direct compare
        if (GenericUtil.isUuid(storedUserId)) {
            return internalAuthUserId.equals(storedUserId.trim());
        }
        // Legacy raw userId — only check if backward compat is enabled
        if (!piiBackwardCompatibility) {
            return false;
        }
        try {
            String internalStoredUserId = getOrCreateInternalUserId(storedUserId);
            if (internalStoredUserId != null) {
                return internalAuthUserId.equals(internalStoredUserId);
            }
        } catch (UserLookupException ex) {
            LOGGER.warn("UUID resolution failed for stored user during match for masked user {}", GenericUtil.maskIdentifier(storedUserId));
            return false;
        }
        return false;
    }

    /**
     * Creates a canonical user row for the given identifier. Idempotent — concurrent inserts
     * are handled by catching DataIntegrityViolationException and re-fetching.
     */
    @Transactional
    public UserDetails createInternalUser(String userId, String standardizedUserId, String userIdHash) {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(UUID.randomUUID());
        userDetails.setIdentifierHash(userIdHash);
        userDetails.setCrDtimes(LocalDateTime.now());
        userDetails.setIdentifierEncrypted(encryptUserIdRequired(userId));
        userDetails.setEncryptedDtimes(LocalDateTime.now());
        LOGGER.info("Creating new internal user mapping for masked identifier {}", GenericUtil.maskIdentifier(standardizedUserId));
        try {
            return userDetailsRepository.save(userDetails);
        } catch (DataIntegrityViolationException ex) {
            return userDetailsRepository.findByIdentifierHash(userIdHash).orElseThrow(() -> ex);
        }
    }

}
