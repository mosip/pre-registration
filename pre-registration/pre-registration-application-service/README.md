# pre-registration-application-service

This service manages to provide the following service to the Pre-registration application.

  * Demographic - This service details used by Pre-Registration portal to maintain the demographic data by providing his/her basic details.
  * Document- This service enables Pre-Registration portal to request for uploading the document for a particular pre-registration.
  * QrCodeGenerator- This service details used by Pre-Registration portal to generate QR Code.
  * Transliteration- This service details used by Pre-Registration portal to provide transliteration from primary to secondary language.
  * Notification- This service details used by Pre-Registration portal to trigger notification via SMS or email.
  * Login - This service details used by Pre-Registration portal to authenticate user by sending OTP to the user, validating  with userid and OTP.
 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-application-service.md)

 

## Default Port and Context Path

  * server.port=9092
  * server.servlet.context-path=/preregistration/v1



## URL

* https://{dns-name}:9092/preregistration/v1/applications/swagger-ui.html 
* https://{dns-name}:9092/preregistration/v1/appointment/swagger-ui.html 
* https://{dns-name}:9092/preregistration/v1/transliteration/swagger-ui.html 
* https://{dns-name}:9092/preregistration/v1/login/swagger-ui.html
* https://{dns-name}:9092/preregistration/v1/notification/swagger-ui.html 
* https://{dns-name}:9092/preregistration/v1/qrCode/swagger-ui.html 

## Roles to Access the URL

* INDIVIDUAL
* REGISTRATION_OFFICER
* REGISTRATION_SUPERVISOR
* REGISTRATION_ADMIN



## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel | kernel-auth-service| /authorize/validateToken |
| commons/kernel | kernel-pridgenerator-service | /pridgenerator/prid |
| commons/kernel | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
| | /validdocuments/{languagecode} | 
| |  /{langcode}/{templatetypecode} |
| commons/kernel | kernel-emailnotification-service | /email/send |
| commons/kernel | kernel-smsnotification-service | /sms/send |
| commons/kernel | kernel-cryptomanager-service | /encrypt |
| | /decrypt |



