package io.mosip.preregistration.core.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
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
    }

    @Test
    public void testGetDecryptedIdentifierReturnsPlainIdentifier() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(cryptoUtil.encrypt(any(), any())).thenReturn("enc-value".getBytes(StandardCharsets.UTF_8));
        when(cryptoUtil.decrypt(any(), any())).thenReturn("TestUser123".getBytes(StandardCharsets.UTF_8));
        when(userDetailsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDetails saved = userDetailsService.findOrCreateByIdentifier("TestUser123");
        UUID userId = saved.getUserId();
        when(userDetailsRepository.findById(userId)).thenReturn(Optional.of(saved));

        Optional<String> decrypted = userDetailsService.getDecryptedIdentifier(userId);
        assertTrue(decrypted.isPresent());
        assertTrue("TestUser123".equals(decrypted.get()));
    }

    @Test
    public void testGetDecryptedIdentifierReturnsEmptyWhenDecryptFails() {
        UserDetails mock = new UserDetails();
        mock.setUserId(UUID.randomUUID());
        mock.setIdentifierEncrypted("encrypted-payload");
        when(cryptoUtil.decrypt(any(), any())).thenThrow(new RuntimeException("decrypt failure"));
        when(userDetailsRepository.findById(mock.getUserId())).thenReturn(Optional.of(mock));

        Optional<String> decrypted = userDetailsService.getDecryptedIdentifier(mock.getUserId());
        assertFalse(decrypted.isPresent());
    }
}
