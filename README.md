# Pre-Registration 
This repository contains the source code and design documents for MOSIP Pre-Registration module. 

# Dependencies
Pre-Registration services' dependencies are mentioned below.  For all kernel services refer to [commons repo](https://github.com/mosip/commons)

* pre-registration-login-service
    *  kernel-auditmanager-service 
    *  kernel-auth-service 
    *  kernel-config-server 

* pre-registration-demographic-service
    *  kernel-auditmanager-service.
    *  kernel-auth-service  
    *  kernel-config-server  
    *  kernel-cryptomanager-service  
    *  kernel-masterdata-service  
    *  kernel-pridgenerator-service  
	 
* pre-registration-document-service
    *  kernel-auditmanager-service 
    *  kernel-auth-service  
    *  kernel-config-server  
    *  kernel-cryptomanager-service  
    *  kernel-masterdata-service  

* pre-registration-booking-service
    *  kernel-auditmanager-service  
    *  kernel-auth-service  
    *  kernel-config-server   
    *  kernel-masterdata-service  

* pre-registration-notification-service
    *  kernel-auditmanager-service  
    *  kernel-auth-service  
    *  kernel-config-server  
    *  kernel-cryptomanager-service  
    *  kernel-masterdata-service  
    *  kernel-emailnotification-service  
    *  kernel-smsnotification-service  

* pre-registration-datasync-service
    *  pre-registration-demographic-service  
    *  pre-registration-document-service  
    *  pre-registration-booking-service  
    *  kernel-auditmanager-service  
    *  kernel-auth-service  
    *  kernel-config-server  

* pre-registration-generateqrcode-service
    *  kernel-auditmanager-service  
    *  kernel-auth-service
    *  kernel-config-server  

* pre-registration-tranlitration-service
    *  kernel-config-server   

* pre-registration-batchjob
     *  pre-registration-notification-service 
     *  pre-registration-booking-service 
     *  kernel-auditmanager-service  
     *  kernel-auth-service  
     *  kernel-config-server  
     *  kernel-masterdata-service

# Build
The project requires JDK 1.8. 
1. To build jars:
    ```
    $ cd pre-registration
    $ mvn clean install 
    ```
1. To skip JUnit tests and Java Docs:
    ```
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true
    ```
1. To build Dockers
    

# Deploy
1. To run all services as Dockers using MiniKube run [sandbox installer](https://githbu.com/mosip/mosip-infra/deployment/sandbox/) scripts.

1. To run a Service jar individually:
    ```
    `java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar`
    ```
    Example:  
        _profile_: `env` (extension used on configuration property files*)    
        _config_label_: `master` (git branch of config repo*)  
        _config-url_: `http://localhost:51000` (Url of the config server*)  
	
	*Refer to kernel-config-server README for details


1. Note that you will have to run the dependent Services like kernel-config-server to run any Service successfully.

1. To run a Docker image individually:
    ``` 
    $ docker run -it -p <host-port>:<container-port> -e active_profile_env={profile} -e spring_config_label_env= {branch} -e spring_config_url_env={config_server_url} <docker-registry-IP:docker-registry-port/<dcker-image>`
    ```

# Test

# APIs

# Documentation

MOSIP documentation is available on [Wiki](https://github.com/mosip/documentation/wiki)

# License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)

