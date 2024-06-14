package io.mosip.preregistration.datasync.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Config for Data sync
 * 
 * @author M1046129 - Jagadishwari
 *
 */
@Configuration
@ConfigurationProperties("mosip")
public class DataSyncConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSyncConfig.class);
    // Commenting it as its used from Pre-registration-application service
    /** The id. */
//    private Map<String, String> id;
//
//    /**
//     * Sets the id.
//     *
//     * @param id the id
//     */
//    public void setId(Map<String, String> id) {
//        this.id = id;
//    }
//
//    /**
//     * Id.
//     *
//     * @return the map
//     */
//    @Bean
//    public Map<String, String> ic() {
//        return Collections.unmodifiableMap(id);
//    }

    /**
     * @return docket
     */
    @Value("${application.env.local:false}")
    private Boolean localEnv;
    @Value("${swagger.base-url:#{null}}")
    private String swaggerBaseUrl;
    @Value("${server.port:9094}")
    private int serverPort;
    String proto = "http";
    String host = "localhost";
    int port = -1;
    String hostWithPort = "localhost:9094";
//    private OpenApiProperties openApiProperties;
//
//    @Autowired
//    public DataSyncConfig(OpenApiProperties openApiProperties) {
//        this.openApiProperties = openApiProperties;
//    }

    private Set<String> protocols() {
        Set<String> protocols = new HashSet<>();
        protocols.add(proto);
        return protocols;
    }

//    @Bean
//    public OpenAPI openApi() {
//        OpenAPI api = new OpenAPI().components(new Components())
//                .info(new Info().title(openApiProperties.getInfo().getTitle())
//                        .version(openApiProperties.getInfo().getVersion())
//                        .description(openApiProperties.getInfo().getDescription())
//                        .license(new License().name(openApiProperties.getInfo().getLicense().getName())
//                                .url(openApiProperties.getInfo().getLicense().getUrl())));
//        openApiProperties.getService().getServers().forEach(
//                server -> api.addServersItem(new Server().description(server.getDescription()).url(server.getUrl())));
//        return api;
//    }
//
//    @Bean
//    public GroupedOpenApi groupedOpenApi() {
//        return GroupedOpenApi.builder().group(openApiProperties.getGroup().getName())
//                .pathsToMatch(openApiProperties.getGroup().getPaths().stream().toArray(String[]::new)).build();
//    }
}