package io.mosip.preregistration.core.util;

import com.fasterxml.uuid.Generators;

/**
 * UUID Generator
 * 
 * @version 1.0.0
 * @author M1043226
 *
 */
public class UUIDGeneratorUtil {

	/**
	 * Creates a uuid generator util.
	 */
	public UUIDGeneratorUtil() {
	}

	/**
	 * Performs generate id for MOSIP Pre-Registration.
	 *
	 * @return generate id result
	 */
	public static String generateId() {
		return Generators.timeBasedGenerator().generate().toString();
	}
}
