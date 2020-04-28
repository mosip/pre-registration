# pre-registration-document-service:

This service enables Pre-Registration portal to request for uploading the document for a particular pre-registration.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-document-service.md)

 

## Default Port and Context Path

  * server.port=9093
  * server.servlet.context-path=/preregistration/v1



## URL

* https://{dns-name}:9093/preregistration/v1/documents/swagger-ui.html 

## Roles to Access the URL

* INDIVIDUAL
* REGISTRATION_OFFICER
* REGISTRATION_SUPERVISOR
* REGISTRATION_ ADMIN



## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel | kernel-cryptomanager-service | /encrypt |
|  |  | /decrypt |
| commons/kernel  | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
|  |   | /validdocuments/{languagecode} |
| commons/kernel | kernel-auth-service | /authenticate/clientidsecretkey |
