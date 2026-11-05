package io.mosip.preregistration.pipeline;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * Unit tests for {@link InProcessApplicationClient}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class InProcessApplicationClientTest {

	private static final String APPLICATION_ID = "98765432101234";

	@InjectMocks
	private InProcessApplicationClient client;

	@Mock
	private ApplicationServiceIntf applicationService;

	@Test
	public void getApplicationStatusDelegatesToApplicationService() {
		MainResponseDTO<String> expected = new MainResponseDTO<>();
		expected.setResponse("Pending_Appointment");
		when(applicationService.getApplicationStatus(APPLICATION_ID)).thenReturn(expected);

		assertSame(expected, client.getApplicationStatus(APPLICATION_ID));
		verify(applicationService).getApplicationStatus(APPLICATION_ID);
	}
}
