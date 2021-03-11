package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OTPGenerateRequestDTO {
	
	private String id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private Date requesttime;
	
	private String version;
	
	private Map metadata=new HashMap();
	
	private RequestDTO request;


	public Date getRequesttime() {
		return requesttime!=null ? new Date(requesttime.getTime()):null;
	}
	public void setRequesttime(Date requesttime) {
		this.requesttime =requesttime!=null ? new Date(requesttime.getTime()):null;
	}
	
}

