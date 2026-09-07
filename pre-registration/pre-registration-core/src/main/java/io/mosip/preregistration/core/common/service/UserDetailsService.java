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
    private final UserDetailsTxHelper userDetailsTxHelper;

    public UserDetailsService(UserDetailsRepository userDetailsRepository, CryptoUtil cryptoUtil,
            UserDetailsTxHelper userDetailsTxHelper) {
        this.userDetailsRepository = userDetailsRepository;
        this.cryptoUtil = cryptoUtil;
        this.userDetailsTxHelper = userDetailsTxHelper;
    }

    private String standardize(String userId) {
        if (userId == null) {
            return "";
        }
        return userId.trim().toLowerCase();
    }

    private String sha256Hex(String input) {
        try {
            return HMACUtils2.digestAsPlainText(input.getBytes());
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
        // A UUID is already a canonical id; re-registering it would mint a second mapping keyed on
        // the hash of the UUID itself, orphaning the original identifier.
        if (GenericUtil.isUuid(userId)) {
            return userId.trim();
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

    /**
     * Recovers the original identifier behind a canonical user id, by decrypting the copy held in
     * {@code user_details}.
     *
     * <p>This is the reverse of {@link #getOrCreateInternalUserId} and completes the registry's
     * documented contract: the table stores {@code identifier_encrypted} precisely so an ownership
     * column that now holds a UUID can still yield a real address when one is needed — see the
     * {@code user_details} DDL, "Recover: decrypt from user_details for notifications/audit". Without
     * it, replacing a contact identifier with a UUID silently destroys the ability to contact that
     * user.
     *
     * <p><b>Returns plaintext PII.</b> Call it only at the point of use, never log the result, and do
     * not place it on a response. Failures return {@code null} rather than throwing, so a caller
     * degrades to "no address available" instead of failing the operation around it.
     *
     * @param canonicalUserId a canonical UUID; anything else returns {@code null}
     * @return the original identifier, or {@code null} if unknown or not recoverable
     */
    public String recoverIdentifier(String canonicalUserId) {
        if (canonicalUserId == null || !GenericUtil.isUuid(canonicalUserId)) {
            return null;
        }
        try {
            return userDetailsRepository.findById(UUID.fromString(canonicalUserId.trim()))
                    .map(this::decryptIdentifier)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed recovering identifier for canonical user {}",
                    GenericUtil.maskIdentifier(canonicalUserId), ex);
            return null;
        }
    }

    private String decryptIdentifier(UserDetails userDetails) {
        byte[] decrypted = cryptoUtil.decrypt(
                userDetails.getIdentifierEncrypted().getBytes(StandardCharsets.UTF_8),
                userDetails.getEncryptedDtimes());
        if (decrypted == null || decrypted.length == 0) {
            LOGGER.error("Empty decrypted identifier for canonical user {}",
                    GenericUtil.maskIdentifier(String.valueOf(userDetails.getUserId())));
            return null;
        }
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Resolves an identifier to its canonical id <em>without</em> registering one that does not exist.
     *
     * <p>The read-only counterpart to {@link #getOrCreateInternalUserId}. That method's create path
     * encrypts through the crypto service — a blocking remote call — and inserts a row, which is
     * appropriate on a write path handling one identity, but not on a read path that resolves an
     * identifier per row of a listing. Use this wherever a stored identifier is being rendered rather
     * than recorded: an unregistered value yields {@code null} so the caller can omit the field,
     * instead of minting a registry entry as a side effect of someone running a query.
     *
     * <p>Hashing is local, so this costs one indexed lookup and never writes. Failures return
     * {@code null} rather than throwing, so one unresolvable row cannot fail the request around it.
     *
     * @param userId a canonical id (returned as-is) or a raw identifier to look up
     * @return the canonical id, or {@code null} if the identifier has no registry entry
     */
    public String findExistingUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String trimmedUserId = userId.trim();
        if (GenericUtil.isUuid(trimmedUserId)) {
            return trimmedUserId;
        }
        try {
            String userHash = sha256Hex(standardize(trimmedUserId));
            return userDetailsRepository.findByIdentifierHash(userHash)
                    .map(userDetails -> userDetails.getUserId().toString())
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed looking up internal user ID for masked user {}",
                    GenericUtil.maskIdentifier(trimmedUserId), ex);
            return null;
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

    /**
     * Compares an authenticated user against the identifier stored on a record.
     *
     * <p><b>Not side-effect free.</b> Resolution goes through {@link #getOrCreateInternalUserId},
     * so calling this can insert a {@code user_details} row for an identifier that has none yet —
     * for the authenticated user, and (when {@code piiBackwardCompatibility} is enabled) for the
     * legacy raw identifier held on the record. The row is a hash-to-UUID mapping, not a credential
     * or an authorisation grant, and the same row would be created by the caller's next write
     * anyway; this only creates it earlier. Callers should not treat this as a read-only query.
     *
     * <p>Resolving rather than merely looking up is deliberate: a lookup-only comparison would
     * return no match whenever <em>neither</em> side has been registered yet, which is exactly the
     * legacy case backward compatibility exists to serve — two occurrences of the same raw
     * identifier must compare equal, and they only do so once both resolve to the same UUID.
     *
     * <p>A canonical-looking {@code storedUserId} is compared directly without confirming it exists
     * in {@code user_details}. When {@code authUserId} is a raw identifier that is sound on its own:
     * resolution goes through the lookup-or-create branch, so {@code internalAuthUserId} is by
     * construction a registered UUID, an unregistered stored value cannot equal it, and the
     * comparison fails closed.
     *
     * <p>Note the one case where that reasoning does not hold. {@link #getOrCreateInternalUserId}
     * returns UUID-shaped input verbatim without consulting {@code user_details}, so if the token
     * subject is itself UUID-shaped, {@code internalAuthUserId} is unverified and this degrades to
     * string equality between the token subject and the stored value. That is safe only for as long
     * as authenticated subjects are never UUID-shaped, which is a property of the identity provider
     * and is not enforced here.
     *
     * @param authUserId              identifier of the authenticated caller
     * @param storedUserId            identifier persisted on the record being checked
     * @param piiBackwardCompatibility whether legacy raw stored identifiers may still be matched
     * @return {@code true} only if both resolve to the same canonical user
     */
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
     * Creates a canonical user row for the given identifier. Idempotent — a concurrent insert that
     * wins the race on {@code identifier_hash} surfaces as a {@code DataIntegrityViolationException},
     * which is caught here and resolved by re-reading the winner's row.
     *
     * <p>The insert is delegated to {@link UserDetailsTxHelper} rather than performed here, and this
     * method is deliberately <em>not</em> a transaction boundary itself. Both details are load
     * bearing: the helper is a separate bean so the Spring proxy actually applies (a same-class call
     * would bypass it), and its {@code REQUIRES_NEW} propagation confines the constraint violation's
     * transaction abort to the inner transaction, so the re-read below still runs on a usable one.
     */
    public UserDetails createInternalUser(String userId, String standardizedUserId, String userIdHash) {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(UUID.randomUUID());
        userDetails.setIdentifierHash(userIdHash);
        userDetails.setCrDtimes(LocalDateTime.now());
        userDetails.setIdentifierEncrypted(encryptUserIdRequired(userId));
        userDetails.setEncryptedDtimes(LocalDateTime.now());
        LOGGER.info("Creating new internal user mapping for masked identifier {}", GenericUtil.maskIdentifier(standardizedUserId));
        try {
            return userDetailsTxHelper.saveInNewTransaction(userDetails);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race; the inner transaction rolled back, so this one can still read the winner.
            return userDetailsRepository.findByIdentifierHash(userIdHash).orElseThrow(() -> ex);
        }
    }

}
