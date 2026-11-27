package io.mosip.preregistration.application.exception.util;

import org.junit.Test;

import io.mosip.preregistration.core.common.dto.MainResponseDTO;

public class DemographicExceptionCatcherTest {

	@Test
	public void unmappedNullPointerExceptionIsLoggedAndNotRethrown() {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		new DemographicExceptionCatcher().handle(new NullPointerException("requiredFields"), response);
	}
}
