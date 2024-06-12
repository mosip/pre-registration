package io.mosip.preregistration.application.constant;

public class PreRegLoginConstant {
	private PreRegLoginConstant() {
		throw new IllegalStateException("PreRegLoginConstant class");
	}
	
	public static final String OTP_SMS_TEMPLATE = "pre.reg.login.otp.sms.template";
	public static final String OTP_SUBJECT_TEMPLATE = "pre.reg.login.otp.mail.subject.template";
	public static final String OTP_CONTENT_TEMPLATE = "pre.reg.login.otp.mail.content.template";
	public static final String SESSION_ID = "sessionId";
	public static final String ACTIVE_STATUS = "active";
	public static final String PHONE_NUMBER = "phone";
	public static final String EMAIL = "email";
	public static final String SMS_NOTIFICATION_SERVICE = "sms-notification";
	public static final String MAIL_NOTIFICATION_SERVICE = "mail-notification";
	public static final String ERRORS = "errors";
	public static final String KEY_SPLITTER = "mosip.kernel.data-key-splitter";
	public static final String MOSIP_KERNEL_OTP_EXPIRY_TIME = "mosip.kernel.otp.expiry-time";
	public static final String MOSIP_PRE_REG_CLIENTID = "mosip.pre.reg.clientId";
	public static final String USED_STATUS = "used";
	public static final String SUCCESS = "success";
	public static final String SMS_SUCCESS = "Sms Request Sent";
	public static final String EMAIL_SUCCESS = "Email Request submitted";
	public static final String VALIDATION_SUCCESS = "VALIDATION_SUCCESSFUL";
	public static final String VALIDATION_UNSUCCESS = "VALIDATION_UNSUCCESSFUL";
	public static final String UNSUCCESS = "failure";
	public static final String VALIDATE_ERROR_MESSAGE = "Validation can't be performed against this key. Generate OTP first.";
	public static final String VALIDATE_ERROR_CODE = "KER-OTV-005";
	public static final String MOSIP_PRIMARY_LANGUAGE = "mosip.primary-language";
}