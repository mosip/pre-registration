package io.mosip.preregistration.application.dto;

/**
 * @author Kishan Rathore
 *
 */
public class QRCodeResponseDTO {
	/**
	 * 
	 */
	byte[] qrcode;

	/**
	 * Sets qrcode.
	 *
	 * @param qrcode qrcode used by set qrcode
	 */
	public void setQrcode(byte[] qrcode) {
		this.qrcode = qrcode != null ? qrcode.clone() : null;
	}

	/**
	 * Returns qrcode.
	 *
	 * @return get qrcode result
	 */
	public byte[] getQrcode() {
		return qrcode != null ? qrcode.clone() : null;
	}
}
