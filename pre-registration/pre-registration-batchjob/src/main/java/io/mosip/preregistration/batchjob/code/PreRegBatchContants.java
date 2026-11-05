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

    /**
     * errors used by this Pre-Registration type.
     */
    public static final String ERRORS = "errors";

    /**
     * response used by this Pre-Registration type.
     */
    public static final String RESPONSE = "response";
	
	/**
	 * zero kiosk used by this Pre-Registration type.
	 */
	public static final short ZERO_KIOSK = 0;

	/**
	 * notification pre reg id used by this Pre-Registration type.
	 */
	public static final String NOTIFICATION_PRE_REG_ID = "mosip.pre-registration.notification.notify";

	/**
	 * notification pre reg ver used by this Pre-Registration type.
	 */
	public static final String NOTIFICATION_PRE_REG_VER = "1.0";

	/**
	 * notification req dto used by this Pre-Registration type.
	 */
	public static final String NOTIFICATION_REQ_DTO = "NotificationRequestDTO";

	/**
	 * lang code used by this Pre-Registration type.
	 */
	public static final String LANG_CODE = "langCode";

	/**
	 * processed status comments used by this Pre-Registration type.
	 */
	public static final String PROCESSED_STATUS_COMMENTS = "Processed by registration processor";

	/**
	 * new status comments used by this Pre-Registration type.
	 */
	public static final String NEW_STATUS_COMMENTS = "Application consumed";

	/**
	 * application consumed job used by this Pre-Registration type.
	 */
	public static final String APPLICATION_CONSUMED_JOB = "ApplicationConsumedJob";

	/**
	 * identity reconciliation job used by this Pre-Registration type.
	 */
	public static final String IDENTITY_RECONCILIATION_JOB = "IdentityReconciliationJob";

	/**
	 * expired status job used by this Pre-Registration type.
	 */
	public static final String EXPIRED_STATUS_JOB = "ExpiredStatusJob";

	/**
	 * appointment mismatch job used by this Pre-Registration type.
	 */
	public static final String APPOINTMENT_MISMATCH_JOB = "AppointmentMismatchJob";

	/**
	 * all used by this Pre-Registration type.
	 */
	public static final String ALL = "all";

	/**
	 * page no used by this Pre-Registration type.
	 */
	public static final String PAGE_NO = "?pageNumber=";

	/**
	 * page size used by this Pre-Registration type.
	 */
	public static final String PAGE_SIZE = "&pageSize=10";

	/**
	 * sort by used by this Pre-Registration type.
	 */
	public static final String SORT_BY = "&sortBy=id";

	/**
	 * order by used by this Pre-Registration type.
	 */
	public static final String ORDER_BY = "&orderBy=asc";

	/**
	 * data used by this Pre-Registration type.
	 */
	public static final String DATA = "data";
}
