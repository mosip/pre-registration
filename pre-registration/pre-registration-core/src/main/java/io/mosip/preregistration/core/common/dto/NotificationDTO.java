package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Sanober Noor
 *
 */
@Data
@NoArgsConstructor
@ToString
public class NotificationDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;
	/**
	 * user name
	 */
	private String name;

	/**
	 * Pre-Registration ID
	 */
	private String preRegistrationId;
	/**
	 * appointmentDate
	 */
	private String appointmentDate;
	/**
	 * appointmentTime
	 */
	private String appointmentTime;
	/**
	 * user mobile number
	 */
	private String mobNum;
	/**
	 * user email id
	 */
	private String emailID;
	/**
	 * additionalRecipient for notififcation
	 */
	private boolean additionalRecipient;

	/**
	 * batch config field
	 */
	private Boolean isBatch;

	private String languageCode;

	private List<KeyValuePairDto<String, String>> fullName;

	private List<KeyValuePairDto<String, String>> registrationCenterName;

	private List<KeyValuePairDto<String, String>> address;
}