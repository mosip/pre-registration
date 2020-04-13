# pre-registration-demographic-service

This service details used by Pre-Registration portal to maintain the demographic data by providing his/her basic details.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-demographic-service.md)

 

## Default Port and Context Path

  * server.port=9092
  * server.servlet.context-path=/preregistration/v1



## URL

* https://{dns-name}:9092/preregistration/v1/applications/swagger-ui.html 


## Roles to Access the URL

* INDIVIDUAL
* REGISTRATION_OFFICER
* REGISTRATION_SUPERVISOR
* REGISTRATION_ADMIN



## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel | kernel-pridgenerator-service | /pridgenerator/prid |
| commons/kernel | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
|  |   | /validdocuments/{languagecode} |
| commons/kernel | kernel-auditmanager-service | /audits |
