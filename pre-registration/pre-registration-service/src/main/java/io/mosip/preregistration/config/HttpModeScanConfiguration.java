package io.mosip.preregistration.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.preregistration.core.PreRegistartionCoreApplication;

/**
 * Component-scan boundaries for the HTTP deployment of Pre-Registration.
 * <p>
 * Discovers REST controllers, domain {@code @Service} beans, kernel auth/transliteration
 * adapters, the ID-object validator, ClamAV virus scanner, and khazana object-store
 * adapters. Batchjob types ({@code io.mosip.preregistration.batchjob.*}) are out of this
 * scan — they run only in {@code pre-registration-batchjob}.
 * </p>
 *
 * <h2>Scanned base packages</h2>
 * <ul>
 *   <li>{@code io.mosip.preregistration} — service + core types on the deployable classpath</li>
 *   <li>{@code io.mosip.analytics} — anonymous profile events</li>
 *   <li>{@code io.mosip.kernel.idobjectvalidator} — identity JSON schema validator (loader.path)</li>
 *   <li>{@code io.mosip.kernel.virusscanner} — ClamAV {@code VirusScanner} SPI (loader.path;
 *       Boot 4 ignores that jar's {@code META-INF/spring.factories})</li>
 *   <li>{@code io.mosip.commons.khazana} — object-store adapters</li>
 *   <li>{@code ${mosip.auth.adapter.impl.basepackage}} — typically {@code io.mosip.kernel.auth.defaultadapter}</li>
 *   <li>{@code ${mosip.lang.traslate.adapter.impl.basepackage}} — ICU4J transliteration
 *       ({@code TransliterationImpl} in kernel-core)</li>
 * </ul>
 * <p>
 * Do not scan {@code io.mosip.kernel} as a whole. {@code kernel-core} ships {@code @Component}
 * types whose constructors are not Spring beans and that expect JPA repositories this service
 * does not provide. JDBC uses {@link HibernateDaoConfig} (imported, not scanned) so MOSIP
 * {@code javax.persistence.jdbc.*} properties create the DataSource.
 * </p>
 *
 * @see io.mosip.preregistration.PreRegistrationBootApplication
 */
@Configuration
@Import(HibernateDaoConfig.class)
@ComponentScan(basePackages = {
		"io.mosip.preregistration",
		"io.mosip.analytics",
		"io.mosip.kernel.idobjectvalidator",
		"io.mosip.kernel.virusscanner",
		"io.mosip.commons.khazana",
		"${mosip.auth.adapter.impl.basepackage:io.mosip.kernel.auth.defaultadapter}",
		"${mosip.lang.traslate.adapter.impl.basepackage:io.mosip.kernel.transliteration.icu4j}"
}, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = {
				"io.mosip.preregistration.batchjob..*"
		}),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"io\\.mosip\\.kernel\\.core\\.logger\\.config\\..*",
				"io\\.mosip\\.kernel\\.keymigrate\\..*",
				"io\\.mosip\\.kernel\\.zkcryptoservice\\..*",
				"io\\.mosip\\.kernel\\.tokenidgenerator\\..*",
				"io\\.mosip\\.kernel\\.signature\\..*",
				"io\\.mosip\\.kernel\\.partnercertservice\\..*",
				"io\\.mosip\\.kernel\\.lkeymanager\\..*",
				"io\\.mosip\\.kernel\\.keymanagerservice\\..*",
				"io\\.mosip\\.kernel\\.keymanager\\..*",
				"io\\.mosip\\.kernel\\.keygenerator\\..*",
				"io\\.mosip\\.kernel\\.cryptomanager\\..*",
				"io\\.mosip\\.kernel\\.crypto\\..*",
				"io\\.mosip\\.kernel\\.clientcrypto\\..*"
		}),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				PreRegistartionCoreApplication.class
		})
})
/**
 * Spring configuration for http mode scan configuration in Pre-Registration.
 */
public class HttpModeScanConfiguration {
}
