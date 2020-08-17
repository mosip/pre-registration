package io.mosip.preregistration.application.errorcodes;

/**
 * @author Sanober Noor
 *@since 1.0.0
 */
public enum QrCodeErrorMessages {

	
	/**
	 * @param code
	 * ErrorMessage for PRG_QRC_001
	 */
	
	INPUT_OUTPUT_EXCEPTION("File input output exception"),
	
	/**
	 * ErrorMessage for PRG_QRC_002
	 */
	QRCODE_FAILED_TO_GENERATE("Failed to generate QR code"),
	/**
	 * ErrorMessage for PRG_QRC_004
	 */
	INVALID_REQUESTTIME_FORMAT("Invalid request time format");
	
	private QrCodeErrorMessages(String code) {
		this.code = code;
	}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
