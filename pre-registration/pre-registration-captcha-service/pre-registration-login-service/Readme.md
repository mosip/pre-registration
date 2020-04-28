# pre-registration-login-service

This service details used by Pre-Registration portal to authenticate user by sending OTP to the user, validating with userid and OTP.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-login-service.md)

 

## Default Port and Context Path

  * server.port=9090
  * server.servlet.context-path=/preregistration/v1/login



## URL

* https://{dns-name}:9090/preregistration/v1/login/swagger-ui.html 


## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel | kernel-auth-service| /authorize/validateToken |
| commons/kernel  | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
| commons/kernel |  | /validdocuments/{languagecode} |
| commons/kernel | kernel-auditmanager-service | /audits |
