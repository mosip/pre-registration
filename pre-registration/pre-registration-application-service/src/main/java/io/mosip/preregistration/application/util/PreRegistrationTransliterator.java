///* 
// * Copyright
// * 
// */
//package io.mosip.preregistration.application.util;
//
//import org.springframework.stereotype.Component;
//
//import io.mosip.preregistration.core.spi.translitertor.Translitertor;
//
///**
// * This class provides Transliterator for transliteration application.
// * 
// * @author Kishan Rathore
// * @since 1.0.0
// *
// */
//@Component
//public class PreRegistrationTransliterator implements Translitertor {
//
//	/* (non-Javadoc)
//	 * @see io.mosip.preregistration.core.spi.translitertor.Translitertor#translitrator(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public String translitrator(String languageId, String fromFieldValue) {
//		Transliterator translitratedLanguage = Transliterator.getInstance(languageId);
//		return translitratedLanguage.transliterate(fromFieldValue);
//	}
//
//}
