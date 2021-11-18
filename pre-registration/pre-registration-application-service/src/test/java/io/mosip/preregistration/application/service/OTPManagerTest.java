package io.mosip.preregistration.application.service;

import static org.junit.Assert.*;

import java.io.IOException;

import org.springframework.test.context.ContextConfiguration;

import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { OTPManager.class })
public class OTPManagerTest {

	@InjectMocks
	private OTPManager otpManager;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);	
	}
	
	@Test(expected = PreRegLoginException.class)
	public void testsendOtp() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = null;
		String language = null;
		otpManager.sendOtp(requestDTO,channelType,language);
	}

}
