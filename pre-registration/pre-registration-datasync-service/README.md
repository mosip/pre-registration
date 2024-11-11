# pre-registration-datasync-service

This service enables Pre-Registration to a registration client, request to retrieve all pre-registration ids based on registration client id, appointment date and an user type.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-data-sync-service.md)

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
 
## APIs
API documentation is available [here](https://mosip.github.io/documentation/1.2.0/pre-registration-datasync-service.html).

## Default Port and Context Path

  * server.port=9094
  * server.servlet.context-path=/preregistration/v1



## URL

* https://{dns-name}:9094/preregistration/v1/sync/swagger-ui.html
 

## Roles to Access the URL

* REGISTRATION_OFFICER
* REGISTRATION_SUPERVISOR
* REGISTRATION_ ADMIN
* REGISTRATION_PROCESSOR


## API Dependencies


|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| pre-registration  | pre-registration-booking-service | /appointment/preRegistrationId/{registrationCenterId} |
| pre-registration | pre-registration-document-service | /documents/preregistration/{preRegistrationId}|
| |  | /documents/{documentId}|
| pre-registration  | pre-registration-demographic-service  |  /applications  |
| |  |  /applications/updatedTime |
| commons/kernel  | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
|  | | /validdocuments/{languagecode} |
| commons/kernel | kernel-auditmanager-service | /audits |
