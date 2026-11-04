package io.mosip.preregistration.application.exception;

import java.util.Objects;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Data;

@Data
/**
 * Class supporting appointment execption in MOSIP Pre-Registration (io.mosip.preregistration.application.exception).
 */
public class AppointmentExecption extends BaseUncheckedException {
	/** The Constant serialVersionUID. */	
	private static final long serialVersionUID = 1L;

	/**
	 * The error code associated with the audit failure.
	 */
	private String errorCode;

	/**
	 * A descriptive message explaining the reason for the audit failure.
	 */
	private String errorMessage;

	/**
	 * Creates a appointment execption.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param message message supplied to this constructor
	 */
	public AppointmentExecption(String errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = message;
	}

	@Override
	/**
	 * Compares this instance with another object for equality.
	 *
	 * @param obj obj used by equals
	 * @return equals result
	 */
	public boolean equals(Object obj) {
		super.equals(obj);
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppointmentExecption other = (AppointmentExecption) obj;
		return Objects.equals(errorCode, other.errorCode) && Objects.equals(errorMessage, other.errorMessage);
	}

	@Override
	/**
	 * Indicates whether this instance has h code.
	 *
	 * @return hash code result
	 */
	public int hashCode() {
		super.hashCode();
		return Objects.hash(errorCode, errorMessage);
	}
}
