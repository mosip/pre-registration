package io.mosip.preregistration.demographic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSecretDTO {

	private String clientId;
	private String secretKey;
	private String appId;
}