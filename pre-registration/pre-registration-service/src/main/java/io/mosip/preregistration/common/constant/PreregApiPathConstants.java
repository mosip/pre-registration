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

	/**
	 * Legacy application swagger prefix (relative to {@code /preregistration/v1}).
	 * Catalog: {@code /application-service/swagger-ui.html}.
	 */
	public static final String APPLICATION_SWAGGER_PREFIX = "/application-service";

	/**
	 * Legacy datasync swagger prefix. Catalog:
	 * {@code /sync/datasync-service/swagger-ui.html}.
	 */
	public static final String DATASYNC_SWAGGER_PREFIX = "/sync/datasync-service";

	/**
	 * Legacy captcha swagger prefix. Catalog: {@code /captcha-service/swagger-ui.html}.
	 */
	public static final String CAPTCHA_SWAGGER_PREFIX = "/captcha-service";

	/**
	 * Legacy booking swagger prefix. Catalog:
	 * {@code /appointment/booking-service/swagger-ui.html}.
	 */
	public static final String BOOKING_SWAGGER_PREFIX = "/appointment/booking-service";

	/** Hidden constructor — constants only. */
	private PreregApiPathConstants() {
	}
}
