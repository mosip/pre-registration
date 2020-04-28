/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.demographic.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class is used to define the values for request parameters when
 * performing deletion operarion.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 */
@NoArgsConstructor
@Getter
@ToString
public class DeletePreRegistartionDTO implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

	/** The pre-registration-Id. */
	private String preRegistrationId;

	/** The created by. */
	private String deletedBy;

	/** The create date time. */
	private Date deletedDateTime;
	
	public void setPreRegistrationId(String preRegistrationId) {
		this.preRegistrationId = preRegistrationId;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	public void setDeletedDateTime(Date deletedDateTime) {
		this.deletedDateTime =deletedDateTime !=null ? new Date(deletedDateTime.getTime()) : null;
	}
}
