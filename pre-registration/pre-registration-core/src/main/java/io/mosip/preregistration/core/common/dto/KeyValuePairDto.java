package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.Data;

@Data
/**
 * Data transfer object carrying key value pair across Pre-Registration APIs.
 */
public class KeyValuePairDto<T, R> implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * key used by this Pre-Registration type.
	 */
	private T key;

	/**
	 * value used by this Pre-Registration type.
	 */
	private R value;
}
