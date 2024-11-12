# pre-registration-application-service

This service manages to provide the following service to the Pre-registration application.

  * Demographic - This service details used by Pre-Registration portal to maintain the demographic data by providing his/her basic details.
  * Document- This service enables Pre-Registration portal to request for uploading the document for a particular pre-registration.
  * QrCodeGenerator- This service details used by Pre-Registration portal to generate QR Code.
  * Transliteration- This service details used by Pre-Registration portal to provide transliteration from one language to another language.
  * Notification- This service details used by Pre-Registration portal to trigger notification via SMS or email.
  * Login - This service details used by Pre-Registration portal to authenticate user by sending OTP to the user, validating  with userid and OTP. 

## Build & run (for developers)
Prerequisites:
1. [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)
2. The project requires JDK 21.0.3 and mvn version - 3.9.6
3. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
4. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile

## Configuration
The configuration of the services is controlled by the following property files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
Please refer to the required released tagged version for configuration
:
1. [Configuration-Application](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
2. [Configuration-Pre-Registration](https://github.com/mosip/mosip-config/blob/master/pre-registration-default.properties)

## API Dependencies
	
* kernel-auditmanager-service

* kernel-syncdata-service

* kernel-otpmanager-service

* kernel-notification-service

* kernel-masterdata-service

* kernel-keymanager-service

* kernel-pridgenerator-service

* kernel-auth-service

* keycloak



