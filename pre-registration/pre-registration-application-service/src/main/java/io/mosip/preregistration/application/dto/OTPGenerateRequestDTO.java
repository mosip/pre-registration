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
public class OTPGenerateRequestDTO {

	private String id;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date requesttime;

	private String version;

	private Map metadata = new HashMap();

	private RequestDTO request;

	// Getter and Setter methods for requesttime are overridden manually
	public Date getRequesttime() {
		return requesttime != null ? new Date(requesttime.getTime()) : null;
	}

	public void setRequesttime(Date requesttime) {
		this.requesttime = requesttime != null ? new Date(requesttime.getTime()) : null;
	}
}
