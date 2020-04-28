# pre-registration-notification-service

This service details used by Pre-Registration portal to trigger notification via SMS or email.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-notification-service.md)

 

## Default Port and Context Path

  * server.port=9099
  * server.servlet.context-path=/preregistration/v1/notification



## URL

* https://{dns-name}:9099/preregistration/v1/notification/swagger-ui.html


## Roles to Access the URL

* INDIVIDUAL
* PRE_REGISTRATION_ADMIN



## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel | kernel-emailnotification-service | /email/send |
| commons/kernel | kernel-smsnotification-service | /sms/send |
| commons/kernel  | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
|  |   | /validdocuments/{languagecode} |
|  |   |  /{langcode}/{templatetypecode} |
| commons/kernel | kernel-auditmanager-service | /audits |
