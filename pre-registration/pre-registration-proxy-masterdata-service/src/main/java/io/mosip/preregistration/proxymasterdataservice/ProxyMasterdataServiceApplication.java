package io.mosip.preregistration.proxymasterdataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication 
@ComponentScan(basePackages = "io.mosip.*, ${mosip.auth.adapter.impl.basepackage}")
public class ProxyMasterdataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyMasterdataServiceApplication.class, args);
	}

}
