package io.mosip.preregistration.core.spi.captcha;

/**
 * Contract for i captcha in MOSIP Pre-Registration.
 */
public interface ICaptcha {
	
	Object validateCaptcha(Object obj);

}
