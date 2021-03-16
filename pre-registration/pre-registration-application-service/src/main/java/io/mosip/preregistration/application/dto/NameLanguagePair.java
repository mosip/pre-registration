package io.mosip.preregistration.application.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class NameLanguagePair implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3329383829893549094L;

	private String language_code;
	
	private String value;

}
