package io.mosip.preregistration.datasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Data sync Application
 * 
 * @author M1046129 - Jagadishwari
 *
 */
@ComponentScan(basePackages = { "io.mosip.preregistration.core.*,io.mosip.preregistration.document.*"
		+ ",io.mosip.preregistration.datasync.*, io.mosip.kernel.core.*"
		+ ",io.mosip.kernel.emailnotifier.*,io.mosip.kernel.smsnotifier.*,io.mosip.kernel.cryotomanager.*"
		+ ",io.mosip.kernel.auditmanger.*,io.mosip.kernel.idgenerator.*" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@SpringBootApplication
public class DataSyncApplicationTest {
	public static void main(String[] args) {
		SpringApplication.run(DataSyncApplicationTest.class, args);
	}
}