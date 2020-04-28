package io.mosip.preregistration.datasync.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author M1046129
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class ReverseDatasyncReponseDTO implements Serializable {

	private static final long serialVersionUID = -4160704765944427989L;

	/**
	 * transactionId
	 */
	private String transactionId;

	/**
	 * Count Of Stored PreRegIds
	 */
	private String countOfStoredPreRegIds;

	/**
	 * List of PreRegIds
	 */
	private List<String> preRegistrationIds;

}
