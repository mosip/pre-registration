package io.mosip.preregistration.common.constant;

/**
 * Public URL prefixes preserved after consolidating application, datasync, and captcha
 * into {@code pre-registration-service}.
 * <p>
 * The servlet context is {@code /preregistration/v1}. Application and datasync controllers
 * already map under that context. Captcha used to set context
 * {@code /preregistration/v1/captcha}; {@link io.mosip.preregistration.config.PreregApiPathConfig}
 * restores the {@code /captcha} segment so {@code POST /preregistration/v1/captcha/validatecaptcha}
 * is unchanged.
 * </p>
 *
 * @see io.mosip.preregistration.config.PreregApiPathConfig
 */
public final class PreregApiPathConstants {

	/**
	 * Captcha controller prefix relative to {@code /preregistration/v1}.
	 * Full public path: {@code /preregistration/v1/captcha}.
	 */
	public static final String CAPTCHA_PATH_PREFIX = "/captcha";

	/** Hidden constructor — constants only. */
	private PreregApiPathConstants() {
	}
}
