package io.mosip.preregistration.application.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data transfer object carrying otp generate request across Pre-Registration APIs.
 */
public class OTPGenerateRequestDTO {

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	/**
	 * requesttime used by this Pre-Registration type.
	 */
	private Date requesttime;

	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;

	private Map metadata = new HashMap();

	/**
	 * request used by this Pre-Registration type.
	 */
	private RequestDTO request;

	// Getter and Setter methods for requesttime are overridden manually
	/**
	 * Returns requesttime.
	 *
	 * @return get requesttime result
	 */
	public Date getRequesttime() {
		return requesttime != null ? new Date(requesttime.getTime()) : null;
	}

	/**
	 * Sets requesttime.
	 *
	 * @param requesttime requesttime used by set requesttime
	 */
	public void setRequesttime(Date requesttime) {
		this.requesttime = requesttime != null ? new Date(requesttime.getTime()) : null;
	}
}
