package io.mosip.preregistration.datasync.dto;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author M1046129
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class PreRegistrationIdsDTO implements Serializable {

	private static final long serialVersionUID = 6402670047109104959L;
	@ApiModelProperty(value = "Transaction ID", position = 1)
	/**
	 * transaction id used by this Pre-Registration type.
	 */
	private String transactionId;
	@ApiModelProperty(value = "Count Of PreRegIds", position = 2)
	/**
	 * count of pre reg ids used by this Pre-Registration type.
	 */
	private String countOfPreRegIds;
	@ApiModelProperty(value = "Pre-Registration Ids", position = 3)
	/**
	 * pre registration ids used by this Pre-Registration type.
	 */
	private Map<String,String> preRegistrationIds;
}
