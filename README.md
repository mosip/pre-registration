[![Maven Package upon a push](https://github.com/mosip/pre-registration/actions/workflows/push-trigger.yml/badge.svg?branch=master)](https://github.com/mosip/pre-registration/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=master)](https://sonarcloud.io/dashboard?branch=master)

## Overview

The **Pre-Registration** module enables a resident to:
1. Resident can login into PreRegistration with email id / phone number
2. Resident can create multiple PreRegistration application.
3. For each PreRegistration application, resident can give demographic data in multiple languages.
4. For each PreRegistration application, resident can upload various proof documents.
5. For each PreRegistration application, resident can search for a Registration Center and search for suitable appointment slots.
6. For each PreRegistration application, resident can book an appointment at suitable Registration Center
7. For every appointment booking, resident can get the acknowledgement as well as a notification via email id / phone number.
8. Resident can cancel the appointment or reschedule it as well.

It exposes a set of APIs that enables resident to do Pre-Registration activities efficiently. A reference front-end implementation is available in the [Pre-registration UI](https://github.com/mosip/pre-registration-ui/). 

For a complete functional overview and capabilities, refer to the [official documentation](https://docs.mosip.io/1.2.0/modules/pre-registration).

Pre-registration Developers Guide [here](https://docs.mosip.io/1.2.0/modules/pre-registration/pre-registration-developer-setup)

## Services

Pre-registration module consists of the following services:
1. [Application](https://github.com/mosip/pre-registration/tree/release-1.3.x/pre-registration/pre-registration-application-service) - This service helps to create a Pre Registration application for a resident and it also uses [Pre Registration Booking service](https://github.com/mosip/mosip-ref-impl/tree/release-1.3.x/pre-registration-booking-service) to book appointment.
2. [Batchjob](https://github.com/mosip/pre-registration/tree/release-1.3.x/pre-registration/pre-registration-batchjob) - This cron job runs as per the schedule and helps is many background tasks like creating appointment slots for registration centres, archiving pre-registration applications once workflow is over etc.
3. [Captcha](https://github.com/mosip/pre-registration/tree/release-1.3.x/pre-registration/pre-registration-captcha-service) - *(Deprecated)* - This service is no longer in use.
   **Captcha functionality is now provided by:** - [MOSIP Captcha Service](https://github.com/mosip/captcha/tree/release-0.1.x)
4. [Core](https://github.com/mosip/pre-registration/tree/release-1.3.x/pre-registration/pre-registration-core) - This service contains many reusable components across all Pre Registration services
5. [Datasync](https://github.com/mosip/pre-registration/tree/release-1.3.x/pre-registration/pre-registration-datasync-service) - This service syncs Pre Registration application data with Registration Centre. This service also marks Pre Registration application as consumed (reverse sync) from Registration Processor once the workflow is complete.

## Database

Before starting the local setup, execute the required SQL scripts to initialize the database.

All database SQL scripts are available in the [db_scripts](https://github.com/mosip/pre-registration/tree/release-1.3.x/db_scripts) directory.

## Local Setup
The project can be set up in two ways:

1. [Local Setup (for Development or Contribution)](#local-setup-for-development-or-contribution)
2. [Local Setup with Docker (Easy Setup for Demos)](#local-setup-with-docker-easy-setup-for-demos)

### Prerequisites

Before you begin, ensure you have the following installed:

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **Docker**: Latest stable version
- **PostgreSQL**: 10.2
- **Keycloak**: [Check here](https://github.com/mosip/keycloak)

### Runtime Dependencies
- Add `kernel-auth-adapter.jar` to the classpath, or include it as a Maven dependency — [Download](https://oss.sonatype.org/#nexus-search;gav~~kernel-auth-adapter~1.3.0-SNAPSHOT~~)
- Add `kernel-transliteration-icu4j.jar` to the classpath, or include it as a Maven dependency — [Download](https://oss.sonatype.org/#nexus-search;gav~~kernel-transliteration-icu4j~1.3.0-SNAPSHOT~~)
- Add `kernel-ref-idobjectvalidator.jar` to the classpath, or include it as a Maven dependency — [Download](https://oss.sonatype.org/#nexus-search;gav~~kernel-ref-idobjectvalidator~1.3.0-SNAPSHOT~~)
- Add `kernel-virusscanner-clamav.jar` to the classpath, or include it as a Maven dependency — [Download](https://oss.sonatype.org/#nexus-search;gav~~kernel-virusscanner-clamav~1.3.0-SNAPSHOT~~)

### Configuration
- Pre-Registration module uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
  Please refer to the required released tagged version for configuration.
  [Configuration-PreReg](https://github.com/mosip/mosip-config/blob/master/pre-registration-default.properties) and
  [Configuration-Application](https://github.com/mosip/mosip-config/blob/master/application-default.properties) are defined here. You need to run the config-server along with the files mentioned above.
- For generating clients, refer to MOSIP’s documentation here: [Client Generation Guide](https://docs.mosip.io/1.2.0/interoperability/integrations/mosip-crvs/approach/technical-details#id-1.-create-client-id-role-for-the-crvs)
- To authenticate a client, use the Auth Manager API as described here: [Auth API Documentation](https://docs.mosip.io/1.2.0/interoperability/integrations/mosip-crvs/approach/technical-details#id-2.-fetch-access-token-to-call-the-apis)

#### Required Configuration Properties

The following properties must be configured with your environment-specific values before deployment:

**Database Configuration:**
- `mosip.prereg.database.hostname` - Database hostname (default: postgres-postgresql.postgres)
- `mosip.prereg.database.port` - Database port (default: 5432)
- `db.dbuser.password` - Database user password (passed as environment variable)

**IAM/Keycloak Configuration:**
- `keycloak.internal.url` - Internal Keycloak URL (passed as environment variable)
- `keycloak.external.url` - External Keycloak URL (passed as environment variable)
- `mosip.prereg.client.secret` - PreReg client secret for Keycloak (passed as environment variable)

**Service URLs:**
- `mosip.kernel.authmanager.url` - Auth manager service URL
- `mosip.kernel.prereg-application.url` - PreReg Application service URL
- `mosip.kernel.prereg-datasync.url` - PreReg DataSync service URL
- `mosip.api.internal.url` - Internal API base URL

**Security Configuration:**
- `mosip.security.origins` - Allowed CORS origins (default: localhost:8080)
- `mosip.security.secure-cookie` - Enable secure cookies (default: false)
- `mosip.pre-registration-services.cookie.security` - Cookie security flag (default: true)

**Note**:
- **If using config-server**: Properties marked as environment variables (e.g., `db.dbuser.password`, `keycloak.internal.url`, `keycloak.external.url`, `mosip.prereg.client.secret`) must be passed through the config-server's 'overrides' environment variables and should NOT be defined in property files. Refer to the config-server helm chart for more details.
- **If using application properties directly**: Update these properties directly in your `application.properties` or `pre-registration-default.properties` file with your environment-specific values.

## Installation

### Local Setup (for Development or Contribution)

1. Make sure the config server is running. For detailed instructions on setting up and running the config server, refer to the [MOSIP Config Server Setup Guide](https://docs.mosip.io/1.2.0/modules/registration-processor/registration-processor-developers-guide#environment-setup).

**Note**: Refer to the MOSIP Config Server Setup Guide for setup, and ensure the properties mentioned above in the configuration section are taken care of. Replace the properties with your own configurations (e.g., DB credentials, IAM credentials, URL).

2. Clone the repository:

```text
git clone <repo-url>
cd pre-registration
```
3. Build the project:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

4. Start the application:
    - Click the Run button in your IDE, or
    - Run via command: `java -jar target/specific-service:<$version>.jar`

5. Verify Swagger is accessible at: `http://localhost:8080/preregistration/v1/application-service/swagger-ui/index.html`

### Local Setup with Docker (Easy Setup for Demos)

#### Option 1: Pull from Docker Hub

Recommended for users who want a quick, ready-to-use setup — testers, students, and external users.

Pull the latest pre-built images from Docker Hub using the following commands:
```text
docker pull mosipid/pre-registration-application-service:1.3.0-beta.2
docker pull mosipid/pre-registration-datasync-service:1.3.0-beta.2
docker pull mosipid/pre-registration-batchjob:1.3.0-beta.2
docker pull mosipid/pre-registration-booking-service:1.3.0-beta.2
```
#### Option 2: Build Docker Images Locally

Recommended for contributors or developers who want to modify or build the services from source.

1. Clone and build the project:

```text
git clone <repo-url>
cd pre-registration
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

2. Navigate to each service directory and build the Docker image:

```text
cd pre-registration/<service-directory>
docker build -t <service-name> .
```

#### Running the Services

Start each service using Docker:

```text
docker run -d -p <port>:<port> --name <service-name> <service-name>
```

#### Verify Installation

Check that all containers are running:

```text
docker ps
```

Access the services at `http://localhost:<port>` using the port mappings listed above.

## Deployment

### Kubernetes

To deploy Pre-Registration services on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Usage

### Pre-Registration UI

For the complete Pre-Registration UI implementation and usage instructions, refer to the [Pre-Registration UI GitHub repository](https://github.com/mosip/pre-registration-ui/).

## Documentation

For more detailed documents for repositories, you can [check here](https://github.com/mosip/documentation/tree/1.2.0/docs).

### API Documentation

API endpoints, base URL, and mock server details are available via Stoplight and Swagger documentation: [MOSIP Pre-Registration Service API Documentation](https://mosip.github.io/documentation/1.2.0/pre-registration-booking-service.html).

### Product Documentation

To learn more about Pre-Registration services from a functional perspective and use case scenarios, refer to our main documentation: [Click here](https://docs.mosip.io/1.2.0/id-lifecycle-management/identity-issuance/pre-registration).

## Testing

Automated functional tests are available in the [Functional Tests repository](api-test).

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/pre-registration/issues)

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).

