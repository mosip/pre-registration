package io.mosip.preregistration.core.spi.translitertor;

/**
 * Contract for translitertor in MOSIP Pre-Registration.
 */
public interface Translitertor {
	/**
	 * Performs translitrator for MOSIP Pre-Registration.
	 *
	 * @param languageId language id used by translitrator
	 * @param formFieldValue form field value used by translitrator
	 * @return translitrator result
	 */
	public String translitrator(String languageId, String formFieldValue);
}
