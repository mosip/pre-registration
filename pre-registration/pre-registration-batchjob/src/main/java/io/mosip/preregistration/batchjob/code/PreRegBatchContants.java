package io.mosip.preregistration.batchjob.code;

/**
 * Constants for PreRegBatch
 * 
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
public class PreRegBatchContants {

    /**
	 * The constant sessionID
	 */
	public static final String SESSIONID = "PreRegBatchSessionId";

    /**
	 * The constant PreRegBatch
	 */
	public static final String PRE_REG_BATCH = "PreRegBatch";

    /**
	 * The constant EMPTY
	 */
	public static final String EMPTY = "";

    public static final String ERRORS = "errors";

    public static final String RESPONSE = "response";
	
	public static final short ZERO_KIOSK = 0;

	public static final String NOTIFICATION_PRE_REG_ID = "mosip.pre-registration.notification.notify";

	public static final String NOTIFICATION_PRE_REG_VER = "1.0";

	public static final String NOTIFICATION_REQ_DTO = "NotificationRequestDTO";

	public static final String LANG_CODE = "langCode";

	public static final String PROCESSED_STATUS_COMMENTS = "Processed by registration processor";

	public static final String NEW_STATUS_COMMENTS = "Application consumed";

	public static final String APPLICATION_CONSUMED_JOB = "ApplicationConsumedJob";

	public static final String EXPIRED_STATUS_JOB = "ExpiredStatusJob";

	public static final String APPOINTMENT_MISMATCH_JOB = "AppointmentMismatchJob";

	public static final String ALL = "all";

	public static final String PAGE_NO = "?pageNumber=";

	public static final String PAGE_SIZE = "&pageSize=10";

	public static final String SORT_BY = "&sortBy=id";

	public static final String ORDER_BY = "&orderBy=asc";

	public static final String DATA = "data";
}
