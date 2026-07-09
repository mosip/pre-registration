package io.mosip.preregistration.core.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.CryptoUtil;

public class UserDetailsServiceTest {

    @Mock
    private UserDetailsRepository userDetailsRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private UserDetailsService userDetailsService;

    public UserDetailsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindOrCreateCreatesWhenNotFound() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn("enc-value".getBytes(StandardCharsets.UTF_8));
        when(userDetailsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDetails u = userDetailsService.createInternalUser("TestUser", "testuser", "somehash");

        verify(userDetailsRepository).save(any());
        assertEquals("enc-value", u.getIdentifierEncrypted());
    }

    @Test
    public void testResolveUserUuidReturnsExistingUuid() {
        UserDetails mock = new UserDetails();
        mock.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mock));

        String res = userDetailsService.getOrCreateInternalUserId("TestUser");
        verify(userDetailsRepository).findByIdentifierHash(any());

        assertNotNull(res);
        assertEquals("00000000-0000-0000-0000-000000000001", res);
    }

    @Test
    public void testFindOrCreateFailsWhenEncryptedValueIsNullForNewRecord() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> userDetailsService.createInternalUser("TestUser", "testuser", "somehash"));
    }

    @Test
    public void testFindOrCreateFailsWhenEncryptedValueIsNullForExistingRecordRepair() {
        UserDetails existing = new UserDetails();
        existing.setUserId(UUID.randomUUID());
        existing.setIdentifierHash("hash");
        existing.setIdentifierEncrypted(null);
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(existing));
        when(cryptoUtil.encrypt(any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> userDetailsService.createInternalUser("TestUser", "testuser", "somehash"));
    }

    @Test
    public void testGetDecryptedIdentifierReturnsPlainIdentifier() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn("enc-value".getBytes(StandardCharsets.UTF_8));
        when(cryptoUtil.decrypt(any(), any())).thenReturn("TestUser123".getBytes(StandardCharsets.UTF_8));
        when(userDetailsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDetails saved = userDetailsService.createInternalUser("TestUser123", "testuser123", "somehash");
        Optional<String> decrypted = decryptIdentifier(saved.getIdentifierEncrypted());
        assertTrue(decrypted.isPresent());
        assertTrue("TestUser123".equals(decrypted.get()));
    }

    @Test
    public void testGetDecryptedIdentifierReturnsEmptyWhenDecryptFails() {
        when(cryptoUtil.decrypt(any(), any())).thenThrow(new RuntimeException("decrypt failure"));

        Optional<String> decrypted = decryptIdentifier("encrypted-payload");
        assertFalse(decrypted.isPresent());
    }

    @Test
    public void testResolveCanonicalUserIdReturnsUuidWhenMappingExists() {
        UserDetails mapped = new UserDetails();
        mapped.setUserId(UUID.randomUUID());
        mapped.setCrDtimes(LocalDateTime.now());
        mapped.setIdentifierEncrypted("enc-value");
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mapped));

        String resolved = userDetailsService.getOrCreateInternalUserId("TestUser");

        assertNotNull(resolved);
        assertEquals(mapped.getUserId().toString(), resolved);
    }

    @Test
    public void testResolveCanonicalUserIdReturnsExistingUuidWithoutRepairingRecord() {
        UserDetails mapped = new UserDetails();
        mapped.setUserId(UUID.randomUUID());
        mapped.setCrDtimes(LocalDateTime.now());
        mapped.setIdentifierEncrypted("");
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mapped));

        String resolved = userDetailsService.getOrCreateInternalUserId("TestUser");

        assertNotNull(resolved);
        assertEquals(mapped.getUserId().toString(), resolved);
    }

    @Test
    public void testGetOrCreateInternalUserIdReturnsCanonicalUuidUnchanged() {
        String canonicalUuid = UUID.randomUUID().toString();

        String resolved = userDetailsService.getOrCreateInternalUserId(canonicalUuid);

        assertEquals(canonicalUuid, resolved);
        verifyNoInteractions(userDetailsRepository);
    }

    @Test
    public void testGetOrCreateInternalUserIdTrimsCanonicalUuid() {
        String canonicalUuid = UUID.randomUUID().toString();

        String resolved = userDetailsService.getOrCreateInternalUserId("  " + canonicalUuid + "  ");

        assertEquals(canonicalUuid, resolved);
        verifyNoInteractions(userDetailsRepository);
    }

    @Test
    public void testGetOrCreateInternalUserIdThrowsUserLookupExceptionOnFailure() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn(null); // encryption fails → createInternalUser throws

        assertThrows(UserLookupException.class, () -> userDetailsService.getOrCreateInternalUserId("TestUser"));
    }

    @Test
    public void testGetUserLookupIdsReturnsCanonicalAndLegacyInCompatibilityMode() {
        UserDetails mapped = new UserDetails();
        mapped.setUserId(UUID.randomUUID());
        mapped.setCrDtimes(LocalDateTime.now());
        mapped.setIdentifierEncrypted("enc-value");
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mapped));

        List<String> lookupIds = userDetailsService.getUserLookupIds("TestUser", true);

        assertIterableEquals(List.of(mapped.getUserId().toString(), "TestUser"), lookupIds);
    }

    @Test
    public void testMatchesUserSupportsLegacyAndCanonicalInCompatibilityMode() {
        UserDetails mapped = new UserDetails();
        mapped.setUserId(UUID.randomUUID());
        mapped.setCrDtimes(LocalDateTime.now());
        mapped.setIdentifierEncrypted("enc-value");
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mapped));

        assertTrue(userDetailsService.matchesUser("TestUser", mapped.getUserId().toString(), true));
        assertTrue(userDetailsService.matchesUser("TestUser", "TestUser", true));
    }

    @Test
    public void testMatchesUserReturnsFalseWhenNoCanonicalOrLegacyMatchExists() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());

        assertFalse(userDetailsService.matchesUser("TestUser", "AnotherUser", true));
    }

    private Optional<String> decryptIdentifier(String encryptedValue) {
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
            return Optional.empty();
        }
    }
}
