package io.mosip.preregistration.application.exception.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.PreRegistrationException;

public class DemographicExceptionCatcherTest {

	@Test
	public void unmappedNullPointerExceptionPopulatesErrors() {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		NullPointerException npe = new NullPointerException("requiredFields");
		PreRegistrationException thrown = assertThrows(PreRegistrationException.class,
				() -> new DemographicExceptionCatcher().handle(npe, response));
		assertEquals("PRG_CORE_REQ_016", thrown.getErrorCode());
		assertEquals("requiredFields", thrown.getErrorText());
	}
}
