package io.mosip.preregistration.config;

import static io.mosip.preregistration.common.constant.PreregApiPathConstants.CAPTCHA_PATH_PREFIX;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.mosip.preregistration.captcha.controller.CaptchaController;

/**
 * Preserves legacy public URL paths after consolidating three MOSIP HTTP services into one JVM.
 * <p>
 * Controllers keep relative mappings. Application and datasync already live under servlet
 * context {@code /preregistration/v1}. Captcha used a dedicated context
 * {@code /preregistration/v1/captcha}; this config adds that extra {@code /captcha} prefix
 * so UI, Registration Client, and {@code api-test} require no URL changes.
 * </p>
 *
 * <h2>Path prefix map</h2>
 * <ul>
 *   <li>{@code /preregistration/v1} — application and datasync controllers (servlet context)</li>
 *   <li>{@code /preregistration/v1/captcha} — {@link CaptchaController}</li>
 * </ul>
 *
 * @see io.mosip.preregistration.PreRegistrationBootApplication
 * @see io.mosip.preregistration.common.constant.PreregApiPathConstants
 */
@Configuration
public class PreregApiPathConfig implements WebMvcConfigurer {

	/**
	 * Registers the captcha path prefix on the Spring MVC path matcher.
	 *
	 * @param configurer MVC path-match configurer supplied by Spring Boot
	 */
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(CAPTCHA_PATH_PREFIX, CaptchaController.class::isAssignableFrom);
	}
}
