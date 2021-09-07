package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class KeyValuePairDto<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private T key;
	
	private T value;

}
