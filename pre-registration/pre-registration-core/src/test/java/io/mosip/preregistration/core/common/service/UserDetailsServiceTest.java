package io.mosip.preregistration.core.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        UserDetails u = userDetailsService.findOrCreateByIdentifier("TestUser");

        verify(userDetailsRepository).save(any());
        assertEquals("enc-value", u.getIdentifierEncrypted());
    }

    @Test
    public void testFindByIdentifierDelegatesToRepo() {
        UserDetails mock = new UserDetails();
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mock));

        Optional<UserDetails> res = userDetailsService.findByIdentifier("TestUser");
        verify(userDetailsRepository).findByIdentifierHash(any());

        assertTrue(res.isPresent());
        assertEquals(mock, res.get());
    }

    @Test
    public void testFindOrCreateFailsWhenEncryptedValueIsNullForNewRecord() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> userDetailsService.findOrCreateByIdentifier("TestUser"));
    }

    @Test
    public void testFindOrCreateFailsWhenEncryptedValueIsNullForExistingRecordRepair() {
        UserDetails existing = new UserDetails();
        existing.setUserId(UUID.randomUUID());
        existing.setIdentifierHash("hash");
        existing.setIdentifierEncrypted(null);
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(existing));
        when(cryptoUtil.encrypt(any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> userDetailsService.findOrCreateByIdentifier("TestUser"));
    }

    @Test
    public void testGetDecryptedIdentifierReturnsPlainIdentifier() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn("enc-value".getBytes(StandardCharsets.UTF_8));
        when(cryptoUtil.decrypt(any(), any())).thenReturn("TestUser123".getBytes(StandardCharsets.UTF_8));
        when(userDetailsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDetails saved = userDetailsService.findOrCreateByIdentifier("TestUser123");
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

        Optional<String> resolved = userDetailsService.resolveUserUuid("TestUser");

        assertTrue(resolved.isPresent());
        assertEquals(mapped.getUserId().toString(), resolved.get());
    }

    @Test
    public void testResolveCanonicalUserIdReturnsExistingUuidWithoutRepairingRecord() {
        UserDetails mapped = new UserDetails();
        mapped.setUserId(UUID.randomUUID());
        mapped.setCrDtimes(LocalDateTime.now());
        mapped.setIdentifierEncrypted("");
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mapped));

        Optional<String> resolved = userDetailsService.resolveUserUuid("TestUser");

        assertTrue(resolved.isPresent());
        assertEquals(mapped.getUserId().toString(), resolved.get());
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
