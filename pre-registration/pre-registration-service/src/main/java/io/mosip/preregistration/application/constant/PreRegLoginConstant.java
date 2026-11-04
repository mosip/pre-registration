package io.mosip.preregistration.application.constant;

/**
 * Class supporting pre reg login constant in MOSIP Pre-Registration (io.mosip.preregistration.application.constant).
 */
public class PreRegLoginConstant {
	/**
	 * Creates a pre reg login constant.
	 */
	private PreRegLoginConstant() {
		throw new IllegalStateException("PreRegLoginConstant class");
	}
	
	/**
	 * otp sms template used by this Pre-Registration type.
	 */
	public static final String OTP_SMS_TEMPLATE = "pre.reg.login.otp.sms.template";
	/**
	 * otp subject template used by this Pre-Registration type.
	 */
	public static final String OTP_SUBJECT_TEMPLATE = "pre.reg.login.otp.mail.subject.template";
	/**
	 * otp content template used by this Pre-Registration type.
	 */
	public static final String OTP_CONTENT_TEMPLATE = "pre.reg.login.otp.mail.content.template";
	/**
	 * session id used by this Pre-Registration type.
	 */
	public static final String SESSION_ID = "sessionId";
	/**
	 * active status used by this Pre-Registration type.
	 */
	public static final String ACTIVE_STATUS = "active";
	/**
	 * phone number used by this Pre-Registration type.
	 */
	public static final String PHONE_NUMBER = "phone";
	/**
	 * email used by this Pre-Registration type.
	 */
	public static final String EMAIL = "email";
	/**
	 * sms notification service used by this Pre-Registration type.
	 */
	public static final String SMS_NOTIFICATION_SERVICE = "sms-notification";
	/**
	 * mail notification service used by this Pre-Registration type.
	 */
	public static final String MAIL_NOTIFICATION_SERVICE = "mail-notification";
	/**
	 * errors used by this Pre-Registration type.
	 */
	public static final String ERRORS = "errors";
	/**
	 * key splitter used by this Pre-Registration type.
	 */
	public static final String KEY_SPLITTER = "mosip.kernel.data-key-splitter";
	/**
	 * mosip kernel otp expiry time used by this Pre-Registration type.
	 */
	public static final String MOSIP_KERNEL_OTP_EXPIRY_TIME = "mosip.kernel.otp.expiry-time";
	/**
	 * mosip pre reg clientid used by this Pre-Registration type.
	 */
	public static final String MOSIP_PRE_REG_CLIENTID = "mosip.pre.reg.clientId";
	/**
	 * used status used by this Pre-Registration type.
	 */
	public static final String USED_STATUS = "used";
	/**
	 * success used by this Pre-Registration type.
	 */
	public static final String SUCCESS = "success";
	/**
	 * sms success used by this Pre-Registration type.
	 */
	public static final String SMS_SUCCESS = "Sms Request Sent";
	/**
	 * email success used by this Pre-Registration type.
	 */
	public static final String EMAIL_SUCCESS = "Email Request submitted";
	/**
	 * validation success used by this Pre-Registration type.
	 */
	public static final String VALIDATION_SUCCESS = "VALIDATION_SUCCESSFUL";
	/**
	 * validation unsuccess used by this Pre-Registration type.
	 */
	public static final String VALIDATION_UNSUCCESS = "VALIDATION_UNSUCCESSFUL";
	/**
	 * unsuccess used by this Pre-Registration type.
	 */
	public static final String UNSUCCESS = "failure";
	/**
	 * validate error message used by this Pre-Registration type.
	 */
	public static final String VALIDATE_ERROR_MESSAGE = "Validation can't be performed against this key. Generate OTP first.";
	/**
	 * validate error code used by this Pre-Registration type.
	 */
	public static final String VALIDATE_ERROR_CODE = "KER-OTV-005";
	/**
	 * mosip primary language used by this Pre-Registration type.
	 */
	public static final String MOSIP_PRIMARY_LANGUAGE = "mosip.primary-language";
}
