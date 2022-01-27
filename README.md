[![Build Status](https://travis-ci.org/mosip/pre-registration.svg?branch=master)](https://travis-ci.org/mosip/pre-registration)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_pre-registration&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_pre-registration)
[![Join the chat at https://gitter.im/mosip-community/Pre-registration](https://badges.gitter.im/mosip-community/Pre-registration.svg)](https://gitter.im/mosip-community/Pre-registration?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Pre-registration 
This repository contains the source code and design documents for MOSIP Pre-registration server.  For an overview refer [here](https://docs.mosip.io/1.2.0/modules/pre-registration).  The modules exposes API endpoints. For a reference front-end UI implementation refer to [Pre-registration UI github repo](https://github.com/mosip/pre-registration-ui/tree/1.2.0-rc2).

Pre-registration module consists of the following services:
1. Application
2. Booking
3. Batchjob
4. Captcha
5. Datasync

# Database

See [DB guide](db_scripts/README.md)

# Config-Server
To run Pre-registration services, there must be a Config server running. [Steps](https://github.com/mosip/mosip-config/tree/develop3-v3)

# Build
The project requires JDK 1.11. 
1. To build jars:
    ```
    $ cd pre-registration
    $ mvn clean install 
    ```
1. To skip JUnit tests and Java Docs:
    ```
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true
    ```
1. To build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

# Deploy

## PreReg in Sandbox
To deploy Pre-registration on Kubernetes cluster using Dockers refer to [mosip-infra](https://github.com/mosip/mosip-infra/tree/1.2.0-rc2/deployment/v3)

## Developer

1. As a developer, to run a service jar individually:
    ```
    `java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar`
    ```
    Example:  
        _profile_: `env` (extension used on configuration property files*)    
        _config_label_: `master` (git branch of config repo*)  
        _config-url_: `http://localhost:51000` (Url of the config server*)  
	
1. Note that you will have to run the dependent services like kernel-config-server to run any service successfully.    

# Configuration
Refer to the [configuration guide](docs/configuration.md).

# Test
Automated functaionl tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests)

# APIs
API documentation available on Wiki: [Pre-registration APIs](https://mosip.github.io/documentation/)

# License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)

