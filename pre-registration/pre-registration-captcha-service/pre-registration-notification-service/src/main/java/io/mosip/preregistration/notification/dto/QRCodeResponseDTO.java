package io.mosip.preregistration.notification.dto;

/**
 * This Dto is used for geting reponse of generate QR code
 * 
 * @author Kishan Rathore
 * @since 1.0.0
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
