package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This DTO class is used to define the values for request parameters when
 * performing deletion operarion.
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeleteBookingDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The pre-registration-Id. */
	private String preRegistrationId;

	/** The created by. */
	private String deletedBy;

	/** The create date time. */
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private Date deletedDateTime;

	public Date getDeletedDateTime() {
		return deletedDateTime!=null? new Date(deletedDateTime.getTime()):null;
	}

	public void setDeletedDateTime(Date deletedDateTime) {
		this.deletedDateTime =deletedDateTime !=null ? new Date(deletedDateTime.getTime()):null;
	}
	
}

