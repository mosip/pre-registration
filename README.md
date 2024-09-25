[![Maven Package upon a push](https://github.com/mosip/pre-registration/actions/workflows/push_trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/pre-registration/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=develop&project=mosip_pre-registration&metric=alert_status)](https://sonarcloud.io/dashboard?branch=develop&id=mosip_pre-registration)

## Note: This branch is created to host updated testrig code for CRE checks.

# Pre-registration 
This repository contains the source code and design documents for MOSIP Pre-registration server.  For an overview refer [here](https://docs.mosip.io/1.2.0/modules/pre-registration).  The modules exposes API endpoints. For a reference front-end UI implementation refer to [Pre-registration UI github repo](https://github.com/mosip/pre-registration-ui/). Pre-registration Developers Guide [here](https://docs.mosip.io/1.2.0/modules/pre-registration/pre-registration-developer-setup)

Pre-registration module consists of the following services:
1. Application
2. Booking
3. Batchjob
4. Datasync
5. Capthca

## Database
See [DB guide](db_scripts/README.md)

## Config-Server
To run Pre-registration services, run [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)

## Build & run (for developers)
Prerequisites:
1. [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)
1. JDK 21 and Java21 Artifactory Jars [here](https://oss.sonatype.org/content/repositories/snapshots/io/mosip/)  
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
1. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```
## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Configuration
Refer to the [configuration guide](docs/configuration.md).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

