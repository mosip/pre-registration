package io.mosip.preregistration.application.test.service;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;
import io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.exception.IllegalParamException;
import io.mosip.preregistration.application.service.GenerateQRcodeService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.ValidationUtil;

@RunWith(JUnit4.class)
@SpringBootTest(classes = { GenerateQRcodeServiceTest.class })

public class GenerateQRcodeServiceTest {

	@InjectMocks
	private GenerateQRcodeService service;

	@Mock
	private ValidationUtil serviceUtil;


	@Mock
	private ValidationUtil validationUtil;
	
	@Mock
	private ObjectMapper mapper;

	@Mock
	private QrCodeGenerator<QrVersion> qrCodeGenerator;

	@Value("${qrversion}")
	private String qrversion;
	
	
	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	private NotificationDTO notificationDTO;
	boolean requestValidatorFlag = false;
	MainResponseDTO<NotificationDTO> responseDTO = new MainResponseDTO<>();
	MainResponseDTO<QRCodeResponseDTO> qrCodeResponseDTO = new MainResponseDTO<>();
	NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
	MainRequestDTO<String> qrcodedto = new MainRequestDTO<>();

	@Before
	public void beforeSet() throws ParseException, JsonProcessingException, org.json.simple.parser.ParseException {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(service, "qrversion", "V25");
		mapper=new ObjectMapper();
		qrcodedto.setId("mosip.pre-registration.qrcode.generate");
		qrcodedto.setVersion("1.0");
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum("1234567890");
		notificationDTO.setEmailID("sanober.noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("22:57");
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		mapper.setDateFormat(df);
		mapper.setTimeZone(TimeZone.getDefault());
		String jsonString = mapper.writeValueAsString(notificationDTO);		
		qrcodedto.setRequest(jsonString);
		qrcodedto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		responseDTO = new MainResponseDTO<>();
		responseDTO.setResponse(notificationDTO);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());

		notificationResponseDTO.setMessage("Notification send successfully");
		notificationResponseDTO.setStatus("True");
	}

	@Test
	public void generateQRCodeSuccessTest() throws QrcodeGenerationException, java.io.IOException {

		QRCodeResponseDTO qrcodeResponseDTO=new QRCodeResponseDTO();
		qrcodeResponseDTO.setQrcode("123".getBytes());
		qrCodeResponseDTO.setResponse(qrcodeResponseDTO);
		qrCodeResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		Mockito.when(qrCodeGenerator.generateQrCode(Mockito.any(), Mockito.any())).thenReturn(qrcodeResponseDTO.getQrcode());
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		MainResponseDTO<QRCodeResponseDTO> response = service.generateQRCode(qrcodedto);
assertTrue(Arrays.equals(qrCodeResponseDTO.getResponse().getQrcode(), response.getResponse().getQrcode()));
//		assertEquals(qrCodeResponseDTO.getResponse().getQrcode(), response.getResponse().getQrcode());
	}

	@Test(expected = InvalidRequestException.class)
	public void generateQRCodeExceptionTest() throws java.io.IOException, QrcodeGenerationException {
		String stringjson = mapper.writeValueAsString(qrcodedto);
		notificationDTO = new NotificationDTO();
		qrcodedto.setRequest(null);
		byte[] qrCode = null;

		QRCodeResponseDTO responsedto = new QRCodeResponseDTO();
		responsedto.setQrcode(qrCode);
		Mockito.when(qrCodeGenerator.generateQrCode(stringjson, QrVersion.V25)).thenReturn(qrCode);
		service.generateQRCode(qrcodedto);
	}
	
	@Test(expected = IllegalParamException.class)
	public void generateQRCodeFailureTest() throws java.io.IOException, QrcodeGenerationException {

		Mockito.when(qrCodeGenerator.generateQrCode(null, QrVersion.V25)).thenThrow(QrcodeGenerationException.class);
		service.generateQRCode(null);

	}
	
	@Test
	public void setupBookingServiceTest() {
		service.setupBookingService();;
	}
}
