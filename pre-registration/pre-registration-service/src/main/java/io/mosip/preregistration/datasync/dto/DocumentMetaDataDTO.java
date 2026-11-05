package io.mosip.preregistration.datasync.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying document meta data across Pre-Registration APIs.
 */
public class DocumentMetaDataDTO implements Serializable {
	/**
	 * constant serial Version UID
	 */
	private static final long serialVersionUID = 3603793077592157890L;
	/**
	 * format of the document
	 */
	private String format;
	/**
	 * type of the document
	 */
	private String type;
	/**
	 * document name
	 */
	private String value;
	/**
	 * document reference Id
	 */
	private String refNumber;
}
