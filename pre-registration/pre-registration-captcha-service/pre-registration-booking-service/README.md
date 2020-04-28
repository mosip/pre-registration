# pre-registration-booking-service

This service details used by Pre-Registration portal to book an appointment by providing his/her basic appointment details

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-booking-service.md)

 

## Default Port and Context Path

  * server.port=9095
  * server.servlet.context-path=/preregistration/v1



## URL

* https://{dns-name}:9095/preregistration/v1/appointment/swagger-ui.html

 

## Roles to Access the URL

* INDIVIDUAL
* REGISTRATION_OFFICER
* REGISTRATION_SUPERVISOR
* REGISTRATION_ ADMIN
* PRE_REGISTRATION_ADMIN

## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| pre-registration  | pre-registration-notification-service| /notify |
| commons/kernel  | kernel-masterdata-service  | /registrationcenters|
|  |   | /documenttypes/{documentcategorycode}/{langcode}|
|  |   | /validdocuments/{languagecode} |
| commons/kernel | kernel-auditmanager-service | /audits |
