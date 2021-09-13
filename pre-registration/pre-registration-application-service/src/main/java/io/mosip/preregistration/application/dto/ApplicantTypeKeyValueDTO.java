package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
public class ApplicantTypeKeyValueDTO<K, V> {

	private K attribute;
	private V value;

}
