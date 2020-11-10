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

	public void setQrcode(byte[] qrcode) {
		this.qrcode = qrcode != null ? qrcode.clone() : null;
	}

	public byte[] getQrcode() {
		return qrcode != null ? qrcode.clone() : null;
	}
}
