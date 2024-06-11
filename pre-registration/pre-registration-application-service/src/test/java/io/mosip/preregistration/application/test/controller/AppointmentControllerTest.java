package io.mosip.preregistration.application.test.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.AppointmentController;
import io.mosip.preregistration.application.service.AppointmentService;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AppointmentController.class)
@Import(AppointmentController.class)
public class AppointmentControllerTest {

	/**
	 * Autowired reference for {@link #MockMvc}
	 */
	private MockMvc mockmvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private RequestValidator requestValidator;

	@MockBean
	private AppointmentService appointmentService;

	@Before
	public void setup() {
		mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void getAvailablitySlotsTest() throws Exception {

		MainResponseDTO<AvailabilityDto> response = new MainResponseDTO<AvailabilityDto>();

		Mockito.when(appointmentService.getSlotAvailablity(Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders
				.get("/applications/appointment/slots/availability/{registrationCenterId}", "10001");
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void getAppointmentDetatisTest() throws Exception {

		MainResponseDTO<BookingRegistrationDTO> response = new MainResponseDTO<BookingRegistrationDTO>();

		Mockito.when(appointmentService.getAppointmentDetails(Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications/appointment/{preRegistrationId}",
				"98765432101234");
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void bookAppointmentForPridTest() throws Exception {

		MainResponseDTO<BookingStatusDTO> response = new MainResponseDTO<BookingStatusDTO>();
		
		HttpHeaders header = new HttpHeaders();
		header.add("User-Agent", "test");

		Mockito.when(appointmentService.makeAppointment(Mockito.anyObject(), Mockito.anyString(), Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders
				.post("/applications/appointment/{preRegistrationId}", "98765432101234")
				.content(
						"{\"id\":\"mosip.pre-registration.booking.book\",\"request\":{\"registration_center_id\":\"10009\",\"appointment_date\":\"2021-08-23\",\"time_slot_from\":\"10:15:00\",\"time_slot_to\":\"10:30:00\"},\"version\":\"1.0\",\"requesttime\":\"2021-08-19T08:09:04.674Z\"}")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).headers(header);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void cancelAppointmentForPridTest() throws Exception {

		MainResponseDTO<CancelBookingResponseDTO> response = new MainResponseDTO<CancelBookingResponseDTO>();

		Mockito.when(appointmentService.cancelAppointment(Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders
				.put("/applications/appointment/{preRegistrationId}", "98765432101234")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void cancelAppointmentInternalForPridTest() throws Exception {

		MainResponseDTO<CancelBookingResponseDTO> response = new MainResponseDTO<CancelBookingResponseDTO>();

		Mockito.when(appointmentService.cancelAppointment(Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders
				.put("/internal/applications/appointment/{preRegistrationId}", "98765432101234")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void deleteAppointmentForPridTest() throws Exception {

		MainResponseDTO<CancelBookingResponseDTO> response = new MainResponseDTO<CancelBookingResponseDTO>();

		Mockito.when(appointmentService.cancelAppointment(Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.delete("/applications/appointment")
				.param("preRegistrationId", "39241705740136").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void multiAppointmentTest() throws Exception {

		MainResponseDTO<BookingStatus> response = new MainResponseDTO<BookingStatus>();
		
		HttpHeaders header = new HttpHeaders();
		header.add("User-Agent", "test");

		Mockito.when(appointmentService.makeMultiAppointment(Mockito.any(), Mockito.anyString())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.post("/applications/appointment").content(
				"{\"id\":\"mosip.pre-registration.booking.book\",\"request\":{\"bookingRequest\":[{\"preRegistrationId\":\"38047351465865\",\"registration_center_id\":\"10001\",\"appointment_date\":\"2021-07-19\",\"time_slot_from\":\"09:00:00\",\"time_slot_to\":\"09:15:00\"}]},\"version\":\"1.0\",\"requesttime\":\"2021-07-12T14:04:58.429Z\"}")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).headers(header);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

}
