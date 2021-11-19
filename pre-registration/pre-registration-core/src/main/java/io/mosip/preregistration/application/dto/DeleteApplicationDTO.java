/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class is used to define the values for request parameters when
 * performing deletion operarion.
 * 
 * @author Mayura D
 * @since 1.2.0
 */
@NoArgsConstructor
@Getter
@ToString
public class DeleteApplicationDTO implements Serializable {

	private static final long serialVersionUID = -8998962848793857075L;

	/** The applicationId. */
	private String applicationId;

	/** The created by. */
	private String deletedBy;

	/** The create date time. */
	private Date deletedDateTime;

	public void setPreRegistrationId(String preRegistrationId) {
		this.applicationId = preRegistrationId;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	public void setDeletedDateTime(Date deletedDateTime) {
		this.deletedDateTime = deletedDateTime != null ? new Date(deletedDateTime.getTime()) : null;
	}
}
