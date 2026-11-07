package io.mosip.preregistration.core.common.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsTxHelperTest {

	@Mock
	private UserDetailsRepository userDetailsRepository;

	@Test
	void saveInNewTransactionDelegatesToRepository() {
		UserDetailsTxHelper helper = new UserDetailsTxHelper(userDetailsRepository);
		UserDetails row = new UserDetails();
		row.setUserId(UUID.randomUUID());
		when(userDetailsRepository.save(row)).thenReturn(row);

		assertSame(row, helper.saveInNewTransaction(row));
		verify(userDetailsRepository).save(row);
	}
}
