package io.mosip.preregistration.application.exception;

import java.util.Objects;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Data;

@Data
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

	public AppointmentExecption(String errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = message;
	}

	@Override
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
	public int hashCode() {
		super.hashCode();
		return Objects.hash(errorCode, errorMessage);
	}
}