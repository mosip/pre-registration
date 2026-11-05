package io.mosip.preregistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import io.mosip.preregistration.config.HttpModeScanConfiguration;
import io.mosip.preregistration.config.PreregApiPathConfig;
import io.mosip.preregistration.config.PreregOpenApiConfig;

/**
 * Spring Boot entry point for the MOSIP Pre-Registration HTTP service.
 * <p>
 * Hosts application (demographic, document, login, appointment proxy), booking-native
 * {@code /appointment/**}, datasync, and captcha REST APIs in one JVM. Shared types live in
 * {@code pre-registration-core}. Appointment slot creation and archival stay in
 * {@code pre-registration-batchjob}.
 * </p>
 * <p>
 * Component scan is imported from {@link HttpModeScanConfiguration}. Empty
 * {@code scanBasePackages} still scans this class's package, which would pick up
 * {@code PreRegistartionCoreApplication} and its {@code io.mosip.*} scan. Scan only
 * {@code io.mosip.preregistration.config} here. Unused entries from kernel-core
 * {@code AutoConfiguration.imports} are listed in {@code excludeName} so Boot 4 does
 * not instantiate them (they are {@code @Component} types, not Boot auto-configs).
 * </p>
 *
 * @see HttpModeScanConfiguration
 * @see PreregApiPathConfig
 * @see PreregOpenApiConfig
 */
@EnableCaching
@SpringBootApplication(scanBasePackageClasses = HttpModeScanConfiguration.class, excludeName = {
		"io.mosip.kernel.idgenerator.vid.impl.VidGeneratorImpl",
		"io.mosip.kernel.idgenerator.vid.util.VidFilterUtils",
		"io.mosip.kernel.idgenerator.tokenid.impl.TokenIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.machineid.impl.MachineIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.regcenterid.impl.RegistrationCenterIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.mispid.impl.MispIdGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil",
		"io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl",
		"io.mosip.kernel.idvalidator.prid.impl.PridValidatorImpl",
		"io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl",
		"io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl",
		"io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl",
		"io.mosip.kernel.idvalidator.mispid.impl.MispIdValidatorImpl",
		"io.mosip.kernel.pdfgenerator.impl.PDFGeneratorImpl",
		"io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl",
		"io.mosip.kernel.websub.api.config.IntentVerificationConfig",
		"io.mosip.kernel.websub.api.config.WebSubClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper"
})
@Import({ HttpModeScanConfiguration.class, PreregApiPathConfig.class, PreregOpenApiConfig.class })
/**
 * Spring Boot entry point for pre registration boot application.
 */
public class PreRegistrationBootApplication {

	/**
	 * Starts the consolidated Pre-Registration HTTP process.
	 *
	 * @param args command-line arguments forwarded to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationBootApplication.class, args);
	}
}
