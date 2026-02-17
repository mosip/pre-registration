package io.mosip.preregistration.core.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;

public class UserDetailsServiceTest {

    @Mock
    private UserDetailsRepository userDetailsRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    public UserDetailsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindOrCreateCreatesWhenNotFound() {
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.empty());
        when(userDetailsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDetails u = userDetailsService.findOrCreateByIdentifier("TestUser");

        verify(userDetailsRepository).save(any());
    }

    @Test
    public void testFindByIdentifierDelegatesToRepo() {
        UserDetails mock = new UserDetails();
        when(userDetailsRepository.findByIdentifierHash(any())).thenReturn(Optional.of(mock));
        Optional<UserDetails> res = userDetailsService.findByIdentifier("TestUser");
        verify(userDetailsRepository).findByIdentifierHash(any());
    }
}
