package io.mosip.preregistration.batchjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.preregistration.core.PreRegistartionCoreApplication;

/**
 * This class is used to define the start of the Booking application.
 * 
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */

@SpringBootApplication(excludeName = {
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
		// Config-server sets referenceValidator to the loader.path SPI. This job does not
		// validate identity JSON, and the batch image does not fetch that jar.
		"io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig",
		"io.mosip.kernel.websub.api.config.IntentVerificationConfig",
		"io.mosip.kernel.websub.api.config.WebSubClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper"
})
@ComponentScan(basePackages = "io.mosip.*", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = { "io\\.mosip\\.kernel\\.core\\.logger\\.config\\..*",
                "io\\.mosip\\.preregistration\\.core\\.PreRegistartionCoreApplication",
                "io\\.mosip\\.kernel\\.zkcryptoservice\\..*",
                "io\\.mosip\\.kernel\\.tokenidgenerator\\..*",
                "io\\.mosip\\.kernel\\.idobjectvalidator\\..*",
                "io\\.mosip\\.kernel\\.applicanttype\\..*",
                "io\\.mosip\\.kernel\\.pdfgenerator\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.vid\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.rid\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.tokenid\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.machineid\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.regcenterid\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.mispid\\..*",
                "io\\.mosip\\.kernel\\.idvalidator\\.prid\\..*",
                "io\\.mosip\\.kernel\\.idvalidator\\.rid\\..*",
                "io\\.mosip\\.kernel\\.idvalidator\\.uin\\..*",
                "io\\.mosip\\.kernel\\.idvalidator\\.vid\\..*",
                "io\\.mosip\\.kernel\\.idvalidator\\.mispid\\..*",
                "io\\.mosip\\.kernel\\.licensekeygenerator\\.misp\\..*",
                "io\\.mosip\\.kernel\\.websub\\..*",
                "io\\.mosip\\.kernel\\.signature\\..*",
                "io\\.mosip\\.kernel\\.partnercertservice\\..*",
                "io\\.mosip\\.kernel\\.lkeymanager\\..*",
                "io\\.mosip\\.kernel\\.keymanagerservice\\..*",
                "io\\.mosip\\.kernel\\.keymanager\\..*",
                "io\\.mosip\\.kernel\\.keygenerator\\..*",
                "io\\.mosip\\.kernel\\.cryptomanager\\..*",
                "io\\.mosip\\.kernel\\.crypto\\..*",
                "io\\.mosip\\.kernel\\.clientcrypto\\..*",
                "io\\.mosip\\.kernel\\.datamapper\\..*",
                "io\\.mosip\\.kernel\\.dataaccess\\.hibernate\\.config\\..*",
        }),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = { HibernateDaoConfig.class,
                PreRegistartionCoreApplication.class })
})
/**
 * Class supporting pre registration batch job in MOSIP Pre-Registration (io.mosip.preregistration.batchjob).
 */
public class PreRegistrationBatchJob {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationBatchJob.class, args);
	}
}
