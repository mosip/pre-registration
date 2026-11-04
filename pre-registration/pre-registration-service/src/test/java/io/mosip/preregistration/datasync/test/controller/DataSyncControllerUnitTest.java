package io.mosip.preregistration.datasync.test.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.datasync.controller.DataSyncController;
import io.mosip.preregistration.datasync.dto.ApplicationsDTO;
import io.mosip.preregistration.datasync.dto.DataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.PreRegArchiveDTO;
import io.mosip.preregistration.datasync.service.DataSyncService;

@RunWith(MockitoJUnitRunner.class)
public class DataSyncControllerUnitTest {

	@InjectMocks
	private DataSyncController dataSyncController;

	@Mock
	private DataSyncService dataSyncService;

	@Test
	public void retrievePreRegistrationsTest() {
		MainResponseDTO<PreRegArchiveDTO> response = new MainResponseDTO<>();
		Mockito.when(dataSyncService.getPreRegistrationData("97285429827016")).thenReturn(response);
		ResponseEntity<MainResponseDTO<PreRegArchiveDTO>> result = dataSyncController
				.retrievePreRegistrations("97285429827016");
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void retrievePreRegistrationsWithMachineIdTest() {
		MainResponseDTO<PreRegArchiveDTO> response = new MainResponseDTO<>();
		Mockito.when(dataSyncService.fetchPreRegistrationData("97285429827016", "10001")).thenReturn(response);
		ResponseEntity<MainResponseDTO<PreRegArchiveDTO>> result = dataSyncController
				.retrievePreRegistrations("97285429827016", "10001");
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void retrieveAllAppointmentsSyncV2Test() {
		MainRequestDTO<DataSyncRequestDTO> request = new MainRequestDTO<>();
		MainResponseDTO<ApplicationsDTO> response = new MainResponseDTO<>();
		Mockito.when(dataSyncService.retrieveAllAppointmentsSyncV2(request)).thenReturn(response);
		ResponseEntity<MainResponseDTO<ApplicationsDTO>> result = dataSyncController
				.retrieveAllAppointmentsSyncV2(request);
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}
}
